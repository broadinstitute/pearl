package bio.terra.pearl.core;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"test", "human-readable-logging"})
public abstract class BaseSpringBootTest {}
