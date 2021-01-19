package edu.intech.meetings.services;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Cette classe doit être l'ancêtre de toutes les classes de tests afin que les
 * tests lancés puissent profiter des initialisation et fermetures globales
 * proposées par la classe {@link TestSetup}.
 *
 * @author martin
 *
 */
@ExtendWith(TestSetup.class)
public abstract class AbstractTest {

}
