package com.sto.autoservice.config;

import com.sto.autoservice.core.entity.RepairService;
import com.sto.autoservice.core.entity.Role;
import com.sto.autoservice.core.entity.User;
import com.sto.autoservice.core.repository.RepairServiceRepository;
import com.sto.autoservice.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Демо-данные для первого запуска (только в пустой БД). Пароль у всех: password123.
 * Отключить: app.demo-data.enabled=false (переменная окружения DEMO_DATA=false).
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
public class DemoDataInitializer implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "password123";

    private final UserRepository userRepository;
    private final RepairServiceRepository serviceRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User ivan = user("master_ivan", "ivan@sto.local", "Иван Петров", "+7 900 111-11-11", Role.ROLE_MASTER);
        User alex = user("master_alex", "alex@sto.local", "Алексей Смирнов", "+7 900 222-22-22", Role.ROLE_MASTER);
        user("client_anna", "anna@mail.local", "Анна Кузнецова", "+7 900 333-33-33", Role.ROLE_CLIENT);
        user("client_oleg", "oleg@mail.local", "Олег Васильев", "+7 900 444-44-44", Role.ROLE_CLIENT);

        // Набор услуг мастера Ивана — ТО и тормоза
        service(ivan, "Замена масла и фильтра", "Слив отработки, замена масляного фильтра, контроль уровня",
                1500, 1, "Полусинтетика 5W-40 (4 л)", 2200, "Синтетика Mobil 1 5W-30 (4 л)", 5200);
        service(ivan, "Замена тормозных колодок", "Передняя или задняя ось, чистка и смазка направляющих",
                2500, 1, "Колодки-аналог (комплект)", 1800, "Колодки Brembo (комплект)", 5500);
        service(ivan, "Диагностика ходовой части", "Осмотр подвески, рулевых тяг, сайлентблоков",
                1200, 1, "Без материалов", 0, "Без материалов", 0);

        // Набор услуг мастера Алексея — двигатель и кузов
        service(alex, "Замена ремня ГРМ", "Ремень, ролики и помпа — по комплекту",
                6000, 2, "Комплект ГРМ (аналог)", 4500, "Комплект ГРМ Gates/оригинал", 11000);
        service(alex, "Шиномонтаж и балансировка", "4 колеса, радиус до R18",
                2000, 1, "Грузики стандартные", 300, "Грузики клеевые премиум", 800);
        service(alex, "Локальный кузовной ремонт", "Рихтовка, шпатлёвка, покраска элемента",
                9000, 4, "Краска эконом-класса", 3000, "Краска по коду производителя", 8500);

        log.info("Demo data created: 2 masters, 2 clients (password: {})", DEMO_PASSWORD);
    }

    private User user(String username, String email, String fullName, String phone, Role role) {
        return userRepository.save(User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(DEMO_PASSWORD))
                .fullName(fullName)
                .phone(phone)
                .role(role)
                .build());
    }

    private void service(User master, String name, String description, int labor, int days,
                         String ecoName, int ecoPrice, String premName, int premPrice) {
        serviceRepository.save(RepairService.builder()
                .master(master)
                .name(name)
                .description(description)
                .laborPrice(BigDecimal.valueOf(labor))
                .durationDays(days)
                .economyMaterialName(ecoName)
                .economyMaterialPrice(BigDecimal.valueOf(ecoPrice))
                .premiumMaterialName(premName)
                .premiumMaterialPrice(BigDecimal.valueOf(premPrice))
                .build());
    }
}
