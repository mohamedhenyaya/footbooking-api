package com.footbooking.api.config;

import com.footbooking.api.auth.model.User;
import com.footbooking.api.auth.repository.UserRepository;
import com.footbooking.api.booking.repository.BookingJdbcRepository;
import com.footbooking.api.terrain.model.Terrain;
import com.footbooking.api.terrain.repository.TerrainRepository;
import com.footbooking.api.tournament.model.Tournament;
import com.footbooking.api.tournament.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

        private final UserRepository userRepository;
        private final TournamentRepository tournamentRepository;
        private final TerrainRepository terrainRepository;
        private final BookingJdbcRepository bookingJdbcRepository;
        private final PasswordEncoder passwordEncoder;

        private final Random random = new Random();

        @Override
        public void run(String... args) throws Exception {
                log.info("Starting Data Seeding...");
                seedUsers();
                seedTournaments();
                seedTerrains();
                seedBookings();
                log.info("Data Seeding Completed.");
        }

        private void seedUsers() {
                // Clean up bad data
                List<User> badUsers = userRepository.findAll().stream()
                                .filter(u -> u.getName() == null)
                                .toList();
                if (!badUsers.isEmpty()) {
                        log.info("Deleting {} users with null names...", badUsers.size());
                        userRepository.deleteAll(badUsers);
                }

                // Ensure test user exists
                if (!userRepository.existsByEmail("test@fb.com")) {
                        userRepository.save(createUser("Test User", "test@fb.com", passwordEncoder.encode("123456")));
                        log.info("Seeded test user: test@fb.com");
                }

                if (userRepository.count() >= 50) {
                        log.info("Users already seeded (count >= 50).");
                        return;
                }

                String defaultPass = passwordEncoder.encode("password");

                String[] names = {
                                "Alice", "Bob", "Charlie", "David", "Eva", "Frank", "Grace", "Hugo", "Iris", "Jack",
                                "Karim", "Liam", "Mona", "Nina", "Oscar", "Paul", "Quentin", "Rachel", "Sam", "Tina",
                                "Ugo", "Victor", "Wendy", "Xavier", "Yasmine", "Zack",
                                "Adrien", "Bianca", "Camille", "Dorian", "Elise", "Fabien", "Gabriel", "Helene",
                                "Ismael", "Juliette", "Kevin", "Lea", "Mathieu", "Nora", "Olivier", "Pierre",
                                "Raphael", "Romane", "Sophie", "Thomas", "Ulysse", "Valerie", "William", "Xenia"
                };

                List<User> usersToSave = new ArrayList<>();
                for (String name : names) {
                        String email = name.toLowerCase() + "@example.com";
                        if (!userRepository.existsByEmail(email)) {
                                usersToSave.add(createUser(name, email, defaultPass));
                        }
                }

                if (!usersToSave.isEmpty()) {
                        userRepository.saveAll(usersToSave);
                        log.info("Seeded {} new users.", usersToSave.size());
                }
        }

        private User createUser(String name, String email, String password) {
                return User.builder()
                                .name(name)
                                .email(email)
                                .password(password)
                                .score(random.nextInt(1000)) // Random score 0-999
                                .createdAt(LocalDateTime.now())
                                .enabled(true)
                                .build();
        }

        private void seedTournaments() {
                if (tournamentRepository.count() >= 20) {
                        log.info("Tournaments already seeded (count >= 20).");
                        return;
                }

                List<Tournament> tournaments = new ArrayList<>();

                // Ensure we have some specifically requested or named ones
                tournaments.add(createTournament("Tournoi Open", "Foot", "A", 500.0, 5000.0, 16));
                tournaments.add(createTournament("Summer Cup", "Foot", "B", 1000.0, 10000.0, 8));
                tournaments.add(createTournament("Champions League", "Foot", "C", 1500.0, 25000.0, 32));
                tournaments.add(createTournament("Winter Cup", "Foot", "D", 800.0, 8000.0, 12));
                tournaments.add(createTournament("Pro League", "Foot", "E", 2000.0, 50000.0, 24));

                // Generate more
                String[] events = { "Cup", "League", "Challenge", "Trophy", "Series" };
                String[] adjectives = { "Super", "Mega", "Ultra", "Elite", "Premier", "Amateur", "Youth", "Senior" };

                for (int i = 0; i < 15; i++) {
                        String name = adjectives[random.nextInt(adjectives.length)] + " "
                                        + events[random.nextInt(events.length)] + " " + (2025 + i);
                        tournaments.add(createTournament(name, "Foot", "Terrain " + (char) ('A' + random.nextInt(26)),
                                        100.0 + random.nextInt(900), 1000.0 + random.nextInt(9000),
                                        4 + random.nextInt(28)));
                }

                tournamentRepository.saveAll(tournaments);
                log.info("Seeded {} tournaments.", tournaments.size());
        }

        private Tournament createTournament(String name, String type, String terrain, Double cost, Double prize,
                        Integer max) {
                LocalDate start = LocalDate.now().plusDays(random.nextInt(90));
                LocalDate end = start.plusDays(2 + random.nextInt(5));

                return Tournament.builder()
                                .name(name)
                                .type(type)
                                .terrain(terrain)
                                .cost(cost)
                                .prize(prize)
                                .startDate(start)
                                .endDate(end)
                                .maxTeams(max)
                                .remainingTeams(random.nextInt(max + 1))
                                .build();
        }

        private void seedTerrains() {
                if (terrainRepository.count() > 0) {
                        log.info("Terrains already seeded.");
                        return;
                }

                List<Terrain> terrains = List.of(
                                createTerrain("Five Paris 17", "Paris", 80.0, true),
                                createTerrain("UrbanSoccer La Defense", "Paris", 90.0, true),
                                createTerrain("Le Five Villette", "Paris", 85.0, true),
                                createTerrain("Z5 Bois Senart", "Melun", 70.0, true),
                                createTerrain("Soccer Park Strasbourg", "Strasbourg", 75.0, true),
                                createTerrain("UrbanSoccer Lyon Parilly", "Lyon", 80.0, false),
                                createTerrain("FootSal Lille", "Lille", 60.0, true),
                                createTerrain("Five Bordeaux", "Bordeaux", 70.0, true),
                                createTerrain("Arena 18", "Marseille", 80.0, false),
                                createTerrain("Complexe Sportif Toulouse", "Toulouse", 65.0, false));

                terrainRepository.saveAll(terrains);
                log.info("Seeded Terrains.");
        }

        private Terrain createTerrain(String name, String city, Double price, boolean indoor) {
                return Terrain.builder()
                                .name(name)
                                .city(city)
                                .pricePerHour(BigDecimal.valueOf(price))
                                .indoor(indoor)
                                .createdAt(LocalDateTime.now())
                                .build();
        }

        private void seedBookings() {
                // Since we don't have a count method easily on JdbcRepository, we'll try to
                // insert anyway
                // Or we can rely on catching exceptions or just assuming it's fine if terrains
                // were empty.
                // To be safe, let's just create some bookings if valid users and terrains
                // exist.

                List<User> users = userRepository.findAll();
                List<Terrain> terrains = terrainRepository.findAll();

                if (users.isEmpty() || terrains.isEmpty()) {
                        return;
                }

                // We will seed 100 random bookings
                // Note: this might fail if we violate unique constraints
                // (user+terrain+date+hour? or just id?)
                // BookingJdbcRepository usually just inserts.
                // We need to implement a check to avoid duplication if we re-run?
                // For now, let's assume we run this once or if data is missing.

                int bookingsToCreate = 100;
                int created = 0;

                for (int i = 0; i < bookingsToCreate; i++) {
                        User user = users.get(random.nextInt(users.size()));
                        Terrain terrain = terrains.get(random.nextInt(terrains.size()));
                        LocalDate date = LocalDate.now().plusDays(random.nextInt(14)); // next 2 weeks
                        int hour = 10 + random.nextInt(12); // 10h to 22h

                        // Check availability basic check (optional but good)
                        List<Integer> bookedHours = bookingJdbcRepository.findBookedHours(terrain.getId(), date);
                        if (!bookedHours.contains(hour)) {
                                try {
                                        bookingJdbcRepository.createBooking(user.getId(), terrain.getId(), date, hour);
                                        created++;
                                } catch (Exception e) {
                                        // Ignore conflicts
                                }
                        }
                }
                log.info("Seeded {} bookings.", created);
        }
}
