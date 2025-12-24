package com.footbooking.config;

import com.footbooking.api.auth.model.User;
import com.footbooking.api.auth.repository.UserRepository;
import com.footbooking.api.tournament.model.Tournament;
import com.footbooking.api.tournament.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TournamentRepository tournamentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedUsers();
        seedTournaments();
    }

    private void seedUsers() {
        if (userRepository.count() > 0)
            return;

        String defaultPass = passwordEncoder.encode("password");

        List<User> users = List.of(
                createUser("Alice", 50, defaultPass),
                createUser("Bob", 45, defaultPass),
                createUser("Charlie", 40, defaultPass),
                createUser("David", 38, defaultPass),
                createUser("Eva", 35, defaultPass),
                createUser("Frank", 30, defaultPass),
                createUser("Grace", 28, defaultPass),
                createUser("Hugo", 25, defaultPass),
                createUser("Iris", 22, defaultPass),
                createUser("Jack", 20, defaultPass),
                createUser("Yaya", 18, defaultPass));

        userRepository.saveAll(users);
    }

    private User createUser(String name, Integer score, String password) {
        return User.builder()
                .name(name)
                .email(name.toLowerCase() + "@example.com")
                .password(password)
                .score(score)
                .createdAt(LocalDateTime.now())
                .enabled(true)
                .build();
    }

    private void seedTournaments() {
        if (tournamentRepository.count() > 0)
            return;

        List<Tournament> tournaments = List.of(
                createTournament("Tournoi Open", "Foot", "A", 500.0, 5000.0, LocalDate.of(2025, 1, 10),
                        LocalDate.of(2025, 1, 12), 16, 4),
                createTournament("Summer Cup", "Foot", "B", 1000.0, 10000.0, LocalDate.of(2025, 2, 5),
                        LocalDate.of(2025, 2, 8), 8, 2),
                createTournament("Champions League", "Foot", "C", 1500.0, 25000.0, LocalDate.of(2025, 3, 1),
                        LocalDate.of(2025, 3, 5), 32, 10),
                createTournament("Winter Cup", "Foot", "D", 800.0, 8000.0, LocalDate.of(2025, 4, 1),
                        LocalDate.of(2025, 4, 3), 12, 6),
                createTournament("Pro League", "Foot", "E", 2000.0, 50000.0, LocalDate.of(2025, 5, 5),
                        LocalDate.of(2025, 5, 10), 24, 8));

        tournamentRepository.saveAll(tournaments);
    }

    private Tournament createTournament(String name, String type, String terrain, Double cost, Double prize,
            LocalDate start, LocalDate end, Integer max, Integer remaining) {
        return Tournament.builder()
                .name(name)
                .type(type)
                .terrain(terrain)
                .cost(cost)
                .prize(prize)
                .startDate(start)
                .endDate(end)
                .maxTeams(max)
                .remainingTeams(remaining)
                .build();
    }
}
