package etape3.equipements.coffee_machine.interfaces;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface CoffeeMachineActuatorCI extends OfferedCI, RequiredCI {
    void turnOn() throws Exception;
    void turnOff() throws Exception;
    void startHeating() throws Exception;
    void stopHeating() throws Exception;
    void setSuspendMode() throws Exception;
    void setEcoMode() throws Exception;
    void setNormalMode() throws Exception;
    void setMaxMode() throws Exception;
    void makeExpresso() throws Exception;
    void fillWater() throws Exception;
}