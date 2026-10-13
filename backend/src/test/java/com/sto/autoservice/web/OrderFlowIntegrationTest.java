package com.sto.autoservice.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Сквозной сценарий: мастер заводит услугу → клиент считает стоимость → оформляет заказ →
 * мастер ведёт заказ по статусам. Работает на H2 (профиль test).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private record Account(long id, String token) {
    }

    private Account register(String prefix, String role) throws Exception {
        String username = prefix + UUID.randomUUID().toString().substring(0, 8);
        String body = objectMapper.writeValueAsString(Map.of(
                "username", username,
                "email", username + "@test.local",
                "password", "secret123",
                "fullName", "Тест " + username,
                "phone", "+7 900 000-00-00",
                "role", role));

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return new Account(json.get("user").get("id").asLong(), json.get("token").asText());
    }

    private long createService(Account master, String name) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "name", name,
                "description", "тест",
                "laborPrice", 1000,
                "durationDays", 2,
                "economyMaterialName", "Эконом",
                "economyMaterialPrice", 500,
                "premiumMaterialName", "Премиум",
                "premiumMaterialPrice", 1500));

        MvcResult result = mockMvc.perform(post("/api/v1/master/services")
                        .header("Authorization", "Bearer " + master.token())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private String orderBody(long masterId, List<Map<String, Object>> items) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "masterId", masterId,
                "carBrand", "Toyota",
                "carModel", "Camry",
                "plateNumber", "A123BC",
                "comment", "Стук спереди",
                "items", items));
    }

    @Test
    @DisplayName("Полный сценарий: услуга → расчёт → заказ → статусы → клиенты мастера")
    void fullFlow() throws Exception {
        Account master = register("m", "ROLE_MASTER");
        Account client = register("c", "ROLE_CLIENT");
        long serviceA = createService(master, "Услуга A");
        long serviceB = createService(master, "Услуга B");

        // Каталог мастера публичен
        mockMvc.perform(get("/api/v1/masters/" + master.id() + "/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        List<Map<String, Object>> items = List.of(
                // A: 1000 + 500 = 1500 (2 дня)
                Map.of("serviceId", serviceA, "materialType", "ECONOMY", "deadlineType", "STANDARD"),
                // B: 1000 × 1.3 + 1500 = 2800 (1 день)
                Map.of("serviceId", serviceB, "materialType", "PREMIUM", "deadlineType", "EXPRESS"));

        mockMvc.perform(post("/api/v1/client/orders/calculate")
                        .header("Authorization", "Bearer " + client.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("masterId", master.id(), "items", items))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(4300.0))
                .andExpect(jsonPath("$.totalDurationDays").value(3));

        MvcResult created = mockMvc.perform(post("/api/v1/client/orders")
                        .header("Authorization", "Bearer " + client.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody(master.id(), items)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.totalPrice").value(4300.0))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andReturn();
        long orderId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        // Мастер видит заказ и ведёт его по статусам
        mockMvc.perform(get("/api/v1/master/orders")
                        .header("Authorization", "Bearer " + master.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // Нельзя перепрыгнуть NEW → COMPLETED
        mockMvc.perform(patch("/api/v1/master/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + master.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/v1/master/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + master.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(patch("/api/v1/master/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + master.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // Клиент видит итоговый статус
        mockMvc.perform(get("/api/v1/client/orders/" + orderId)
                        .header("Authorization", "Bearer " + client.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // Клиент появился в списке клиентов мастера
        mockMvc.perform(get("/api/v1/master/clients")
                        .header("Authorization", "Bearer " + master.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(client.id()))
                .andExpect(jsonPath("$[0].ordersCount").value(1))
                .andExpect(jsonPath("$[0].totalSpent").value(4300.0));
    }

    @Test
    @DisplayName("Услугу другого мастера заказать нельзя; чужие заказы недоступны")
    void mastersAreIsolated() throws Exception {
        Account masterA = register("ma", "ROLE_MASTER");
        Account masterB = register("mb", "ROLE_MASTER");
        Account client = register("c", "ROLE_CLIENT");
        long serviceOfA = createService(masterA, "Услуга мастера A");

        List<Map<String, Object>> items = List.of(
                Map.of("serviceId", serviceOfA, "materialType", "ECONOMY", "deadlineType", "STANDARD"));

        // Выбран мастер B, а услуга принадлежит мастеру A
        mockMvc.perform(post("/api/v1/client/orders")
                        .header("Authorization", "Bearer " + client.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody(masterB.id(), items)))
                .andExpect(status().isBadRequest());

        // Мастер B не видит услуги мастера A в своём кабинете
        mockMvc.perform(get("/api/v1/master/services")
                        .header("Authorization", "Bearer " + masterB.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Права доступа по ролям")
    void roleBasedAccess() throws Exception {
        Account master = register("m", "ROLE_MASTER");
        Account client = register("c", "ROLE_CLIENT");

        // без токена
        mockMvc.perform(get("/api/v1/client/orders")).andExpect(status().isUnauthorized());
        // клиент в кабинете мастера
        mockMvc.perform(get("/api/v1/master/services")
                        .header("Authorization", "Bearer " + client.token()))
                .andExpect(status().isForbidden());
        // мастер в кабинете клиента
        mockMvc.perform(get("/api/v1/client/orders")
                        .header("Authorization", "Bearer " + master.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Валидация: дорогие материалы не могут быть дешевле дешёвых")
    void premiumMustNotBeCheaperThanEconomy() throws Exception {
        Account master = register("m", "ROLE_MASTER");
        String body = objectMapper.writeValueAsString(Map.of(
                "name", "Некорректная",
                "laborPrice", 1000,
                "durationDays", 1,
                "economyMaterialName", "Эконом",
                "economyMaterialPrice", 900,
                "premiumMaterialName", "Премиум",
                "premiumMaterialPrice", 100));

        mockMvc.perform(post("/api/v1/master/services")
                        .header("Authorization", "Bearer " + master.token())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Повторная регистрация с тем же логином — 409")
    void duplicateRegistration() throws Exception {
        String body = "{\"username\":\"dupuser\",\"email\":\"dup@test.local\",\"password\":\"secret123\","
                + "\"fullName\":\"Dup\",\"role\":\"ROLE_CLIENT\"}";
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }
}
