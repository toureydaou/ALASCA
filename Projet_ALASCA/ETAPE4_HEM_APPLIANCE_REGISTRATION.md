# Etape 4: HEM Appliance Registration & Energy Management Architecture

## Overview

This document explains how HEM (Household Energy Manager) integrates with energy sources and controls complex appliances using a registration-based approach for Etape 4.

## 1. Reference Architecture (HEM-2025-etape3)

The reference implementation shows a **HEM with fixed connections to energy sources** but NO dynamic appliance registration.

### 1.1 Reference HEMCyPhy Structure

**Key Fields (lines 130-145 of reference HEMCyPhy.java):**
```java
// Energy sources (infrastructure)
protected ElectricMeterOutboundPort      meterop;        // To monitor consumption/production
protected BatteriesOutboundPort          batteriesop;    // To charge/discharge batteries
protected SolarPanelOutboundPort         solarPanelop;   // To query solar production
protected GeneratorOutboundPort          generatorop;    // To start/stop generator

// Hardcoded heater (pre-first step)
protected boolean                        isPreFirstStep; // Control mode flag
protected AdjustableOutboundPort         heaterop;       // Fixed Heater control (only if isPreFirstStep)
```

**Component Lifecycle (lines 365-409):**
```java
@Override
public synchronized void start() throws ComponentStartException {
    // 1. Create and connect to ElectricMeter
    this.meterop = new ElectricMeterOutboundPort(this);
    this.meterop.publishPort();
    this.doPortConnection(
        this.meterop.getPortURI(),
        ElectricMeterCyPhy.ELECTRIC_METER_INBOUND_PORT_URI,
        ElectricMeterConnector.class.getCanonicalName());

    // 2. Create and connect to Batteries
    this.batteriesop = new BatteriesOutboundPort(this);
    this.batteriesop.publishPort();
    this.doPortConnection(
        this.batteriesop.getPortURI(),
        BatteriesCyPhy.STANDARD_INBOUND_PORT_URI,
        BatteriesConnector.class.getCanonicalName());

    // 3. Create and connect to SolarPanel
    this.solarPanelop = new SolarPanelOutboundPort(this);
    this.solarPanelop.publishPort();
    this.doPortConnection(
        this.solarPanelop.getPortURI(),
        SolarPanelCyPhy.STANDARD_INBOUND_PORT_URI,
        SolarPanelConnector.class.getCanonicalName());

    // 4. Create and connect to Generator
    this.generatorop = new GeneratorOutboundPort(this);
    this.generatorop.publishPort();
    this.doPortConnection(
        this.generatorop.getPortURI(),
        GeneratorCyPhy.STANDARD_INBOUND_PORT_URI,
        GeneratorConnector.class.getCanonicalName());

    // 5. Optional: Connect to hardcoded Heater (only if isPreFirstStep)
    if (this.isPreFirstStep) {
        this.heaterop = new AdjustableOutboundPort(this);
        this.heaterop.publishPort();
        this.doPortConnection(
            this.heaterop.getPortURI(),
            Heater.EXTERNAL_CONTROL_INBOUND_PORT_URI,
            HeaterConnector.class.getCanonicalName());
    }
}
```

### 1.2 Key Insight from Reference

**Line 87 comment states:**
> "Especially, no registration of the components representing the appliances is given"

This means the reference implementation:
- ✅ Connects to energy infrastructure (ElectricMeter, Generator, Batteries, SolarPanel)
- ✅ Can test a hardcoded Heater in pre-first step mode
- ❌ Does NOT implement dynamic appliance registration (CoffeeMachine, Kettle, Laundry registering themselves)

---

## 2. Etape 4 Enhancement: Dynamic Appliance Registration

Etape 4 extends the reference by adding **dynamic registration mechanism** for complex appliances.

### 2.1 Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     HEMEnergyManager                         │
│  (Extends HEMCyPhy + implements EnergyCoordinator)           │
│                                                              │
│  Outbound Connections:                                      │
│  ├─ ElectricMeterCI → ElectricMeterCyPhy (Read meter data) │
│  ├─ BatteriesCI → BatteriesCyPhy (Control batteries)       │
│  ├─ SolarPanelCI → SolarPanelCyPhy (Read solar prod.)      │
│  ├─ GeneratorCI → GeneratorCyPhy (Control generator)       │
│  └─ AdjustableCI → Registered Appliances (Control modes)   │
│                                                              │
│  Inbound Connections:                                       │
│  └─ RegistrationCI ← CoffeeMachine, Kettle, Laundry        │
│     (Appliances register, get reference to HEM)             │
│                                                              │
│  Shared State:                                              │
│  ├─ EnergyStateModel (shared data for coordination)         │
│  ├─ EquipmentRegistry (tracks registered appliances)        │
│  └─ EnergyControlLoopTask (periodic control algorithm)      │
└─────────────────────────────────────────────────────────────┘
     ↑          ↓          ↑          ↓          ↑
     │          │          │          │          │
  METER      BATTERIES   SOLAR    GENERATOR   APPLIANCES
```

### 2.2 Data Flow: HEM Monitoring Energy

**Direction: Energy Sources → ElectricMeterCyPhy → HEMEnergyManager**

#### Step 1: ElectricMeterCyPhy Receives Production Data
```
SolarPanel.getCurrentPowerProductionLevel()
    ↓
ElectricMeterCyPhy.setCurrentPowerProduction(watts)
    ↓
Stored in: AtomicReference<SignalData<Double>> currentPowerProduction
```

When Generator runs:
```
Generator.currentPowerProduction()
    ↓
ElectricMeterCyPhy.setCurrentPowerProduction(solar + generator)
    ↓
Aggregated production in ElectricMeter
```

When Batteries discharge:
```
Batteries.getCurrentPowerConsumption()
    ↓
ElectricMeterCyPhy.addToProduction(batteryPower)
    ↓
ElectricMeter tracks: total production from all sources
```

#### Step 2: ElectricMeterCyPhy Receives Consumption Data
```
CoffeeMachine.getModeConsumption(currentMode)
    ↓
ElectricMeterCyPhy.setCurrentPowerConsumption(watts)
    ↓
Stored in: AtomicReference<SignalData<Double>> currentPowerConsumption

Same for Kettle, Laundry, Fan (all report via SIL electricity models)
```

#### Step 3: HEM Queries Meter Periodically
```
EnergyControlLoopTask.run() (every 10 seconds)
    │
    ├─ Read consumption:
    │  meterop.getCurrentConsumption() → SignalData<Double>
    │  Extract: consumptionWatts
    │
    ├─ Read production:
    │  meterop.getCurrentProduction() → SignalData<Double>
    │  Extract: productionWatts
    │
    ├─ Update shared state model:
    │  energyStateModel.setConsumption(consumptionWatts)
    │  energyStateModel.setSolarProduction(solarProd)
    │  energyStateModel.setGeneratorRunning(running, watts)
    │  energyStateModel.setBatteryState(chargeLevel, charging)
    │
    ├─ Calculate balance:
    │  balance = productionWatts - consumptionWatts (>0 = deficit, <0 = surplus)
    │
    ├─ Make decisions:
    │  if (balance < -500W)  → Start generator, suspend appliances
    │  if (balance > 500W)   → Stop generator, resume appliances
    │  if (balance > 500W)   → Charge batteries
    │
    └─ Apply controls via AdjustableOutboundPorts:
       appliancePort.suspend() / resume() / setMode()
```

---

## 3. Appliance Registration Pattern (Etape 4)

### 3.1 How Appliances Register with HEM

```
┌─────────────────────────────┐
│   CoffeeMachineCyPhy        │
│                             │
│ 1. Create RegistrationOP    │
│    rop = RegistrationOP()   │
│                             │
│ 2. During start():          │
│    rop.connect(             │
│      HEM_REGISTRATION_PORT) │
│                             │
│ 3. Call register():         │
│    rop.register(            │
│      uid,                   │
│      externalControlPort,   │
│      xmlDescriptor)         │
│                             │
│    Sends registration to HEM
└──────────┬──────────────────┘
           │
           ↓
┌─────────────────────────────┐
│   HEMEnergyManager           │
│   (RegistrationInboundPort)  │
│                             │
│ 4. Receives registration    │
│ 5. Parses XML descriptor    │
│ 6. Generates connector      │ (via Javassist)
│    class dynamically        │
│ 7. Creates AdjustableOP     │
│ 8. Connects to appliance    │
│ 9. Stores in registry:      │
│    equipmentRegistry.       │
│    register(uid, port, ...)│
│                             │
│ 10. Returns control         │
│     reference to appliance  │
└─────────────────────────────┘
           ↑
           │
      Bidirectional
      communication
      established
```

### 3.2 Appliance Control Data Flow

**HEM → Appliance (Suspend):**
```
EnergyControlLoopTask detects deficit (balance < -500W)
    │
    ├─ registryEquipment = registry.getSuspendableEquipment()
    │  (sorted by priority: Fan first, then CoffeeMachine, then Kettle)
    │
    ├─ For each equipment by priority:
    │  │
    │  ├─ Call: equipmentPort.suspend()
    │  │         (goes to AdjustableOutboundPort → dynamically generated connector)
    │  │
    │  ├─ Appliance receives: suspend() on its ExternalControlInboundPort
    │  │
    │  ├─ Appliance state changes:
    │  │  ├─ currentMode = SUSPEND (usually 0-3W)
    │  │  ├─ power consumption drops dramatically
    │  │  └─ notifies ElectricMeter (via local SIL electricity model)
    │  │
    │  └─ Repeat until deficit solved or all suspended
    │
    ├─ If deficit still > 1.0A:
    │  └─ generatorop.startGenerator() → Generator starts
    │
    └─ Update state model: energyStateModel.setGeneratorRunning(true, 4500)
```

**HEM ← Appliance (Reporting):**
```
EnergyControlLoopTask detects surplus (balance > 500W)
    │
    ├─ registryEquipment = registry.getSuspendedEquipment()
    │  (sorted by urgency: highest emergency() first)
    │
    ├─ For each suspended equipment by urgency:
    │  │
    │  ├─ Check if surplus enough: balance > consumptionOfAppliance
    │  │
    │  ├─ If yes:
    │  │  │
    │  │  ├─ Call: equipmentPort.resume()
    │  │  │         (goes to AdjustableOutboundPort)
    │  │  │
    │  │  ├─ Appliance receives: resume() on ExternalControlInboundPort
    │  │  │
    │  │  ├─ Appliance state changes:
    │  │  │  ├─ Restore to priorSuspendMode (the mode before suspension)
    │  │  │  ├─ power consumption returns to normal
    │  │  │  └─ notifies ElectricMeter
    │  │  │
    │  │  └─ Repeat until surplus consumed or all resumed
    │  │
    │  └─ If yes: appliance resumes
    │
    ├─ If surplus still available:
    │  └─ batteriesop.startCharging() → Batteries charge
    │
    └─ Update state model: energyStateModel.setBatteryState(level, true)
```

---

## 4. Implementation Requirements for Etape 4

### 4.1 HEMCyPhy Modifications

**Add fields:**
```java
@RequiredInterfaces(required = { AdjustableCI.class, ElectricMeterCI.class,
                                  BatteriesCI.class, SolarPanelCI.class,
                                  GeneratorCI.class })
@OfferedInterfaces(offered = { RegistrationCI.class })
public class HEMCyPhy extends AbstractComponent {

    // Energy sources (from reference)
    protected ElectricMeterOutboundPort meterop;
    protected BatteriesOutboundPort batteriesop;
    protected SolarPanelOutboundPort solarPanelop;
    protected GeneratorOutboundPort generatorop;

    // NEW: Registration ports for dynamic appliance registration
    protected RegistrationInboundPort rcip_coffee;     // CoffeeMachine registration
    protected RegistrationInboundPort rcip_kettle;     // Kettle registration
    protected RegistrationInboundPort rcip_laundry;    // Laundry registration
    protected RegistrationInboundPort rcip_fan;        // Fan registration (optional)

    // Map of registered appliances with their control ports
    protected Map<String, AdjustableOutboundPort> equipmentPorts;
}
```

**Add in start() method:**
```java
// Create registration ports
this.rcip_coffee = new RegistrationInboundPort(
    REGISTRATION_COFFEE_INBOUND_PORT_URI, this);
this.rcip_coffee.publishPort();

this.rcip_kettle = new RegistrationInboundPort(
    REGISTRATION_KETTLE_INBOUND_PORT_URI, this);
this.rcip_kettle.publishPort();

this.rcip_laundry = new RegistrationInboundPort(
    REGISTRATION_LAUNDRY_INBOUND_PORT_URI, this);
this.rcip_laundry.publishPort();
```

**Implement RegistrationCI.register() method:**
```java
@Override
public boolean register(String uid, String portURI, String xmlDescriptor)
    throws Exception {

    // 1. Parse XML descriptor to extract control interface
    // 2. Use ConnectorGenerator to create dynamic connector class
    // 3. Create AdjustableOutboundPort
    // 4. Connect to appliance's externalControlInboundPort
    // 5. Store in equipmentPorts map with uid as key
    // 6. Create EquipmentInfo with priority and constraint
    // 7. Register in equipmentRegistry

    // Return success
    return true;
}

@Override
public void unregister(String uid) throws Exception {
    // 1. Disconnect and unpublish AdjustableOutboundPort
    // 2. Remove from equipmentPorts map
    // 3. Remove from equipmentRegistry
}
```

**Add in shutdown() method:**
```java
this.rcip_coffee.unpublishPort();
this.rcip_kettle.unpublishPort();
this.rcip_laundry.unpublishPort();
```

### 4.2 HEMEnergyManager (Extends HEMCyPhy)

**Key additions:**
```java
public class HEMEnergyManager extends HEMCyPhy implements EnergyCoordinator {

    protected EnergyStateModel energyStateModel;
    protected EquipmentRegistry equipmentRegistry;
    protected EnergyControlLoopTask controlTask;

    @Override
    public synchronized void start() throws ComponentStartException {
        super.start(); // Calls HEMCyPhy.start()
        logMessage("HEM Energy Manager starting with control loop enabled");
    }

    @Override
    public synchronized void execute() throws Exception {
        super.execute();
        if (executionMode.isIntegrationTest() ||
            executionMode.isSILIntegrationTest()) {
            startControlLoop();
        }
    }

    protected void startControlLoop() throws Exception {
        controlTask = new EnergyControlLoopTask(
            this,
            this.meterop,           // From HEMCyPhy
            this.generatorop,       // From HEMCyPhy
            this.batteriesop,       // From HEMCyPhy
            this.solarPanelop,      // From HEMCyPhy
            this.equipmentRegistry, // From HEMEnergyManager
            this.energyStateModel,  // From HEMEnergyManager
            CONTROL_LOOP_VERBOSE
        );

        scheduleTaskAtFixedRateOnComponent(
            controlTask,
            CONTROL_PERIOD_NANOS,
            CONTROL_PERIOD_NANOS,
            TimeUnit.NANOSECONDS);
    }
}
```

### 4.3 Appliance Registration (e.g., CoffeeMachineCyPhy)

**During start() or when ready:**
```java
public class CoffeeMachineCyPhy extends AbstractComponent {

    protected RegistrationOutboundPort rop;
    protected String uid = "CoffeeMachine-1"; // Unique ID

    @Override
    public synchronized void start() throws ComponentStartException {
        super.start();

        // Create and connect registration port
        this.rop = new RegistrationOutboundPort(this);
        this.rop.publishPort();
        this.doPortConnection(
            rop.getPortURI(),
            HEMCyPhy.REGISTRATION_COFFEE_INBOUND_PORT_URI,
            RegistrationConnector.class.getCanonicalName());
    }

    // Called when appliance is ready (e.g., during turnOn())
    public void registerWithHEM() throws Exception {
        String xmlDescriptor = "adapters/coffeeci-descriptor.xml";
        String externalControlPortURI = this.ecip.getPortURI();

        // Register: sends uid, port URI, and XML to HEM
        this.rop.register(uid, externalControlPortURI, xmlDescriptor);
    }

    public void unregisterFromHEM() throws Exception {
        this.rop.unregister(uid);
    }
}
```

---

## 5. Energy Coordination Flow

### 5.1 Deficit Scenario (Appliance Suspension)

```
Time 0s: System balanced
         Production=1000W, Consumption=500W, Balance=+500W

         EnergyStateModel:
         ├─ balanceWatts = 500
         ├─ generatorRunning = false
         ├─ batteriesCharging = false

Time 10s: CoffeeMachine turns ON (1500W)
         Production=1000W, Consumption=1500W, Balance=-500W (DEFICIT!)

         EnergyControlLoopTask.run():
         ├─ Reads meter:
         │  └─ consumption=1500W, production=1000W
         ├─ Updates state model:
         │  └─ energyStateModel.setConsumption(1500)
         ├─ Calculates: balance = 1500 - 1000 = 500W deficit
         ├─ Calls handleDeficit():
         │  ├─ Gets suspendable equipment sorted by priority
         │  │  └─ [Fan(p=7), CoffeeMachine(p=6), ...]
         │  ├─ Suspends Fan: equipmentPorts["Fan-1"].suspend()
         │  │  └─ Fan goes to MODE=LOW (50W)
         │  │  └─ ElectricMeter sees: consumption now 1550W (1500 + 50)
         │  ├─ Still deficit, suspends CoffeeMachine: suspended earlier, skip
         │  ├─ If deficit > 1.0A:
         │  │  └─ generatorop.startGenerator()
         │  │  └─ Generator: 4500W
         │  │  └─ New production = 1000 + 4500 = 5500W
         │  └─ Remaining deficit now: -3950W (SURPLUS!)
         │
         └─ Updates state model:
            └─ energyStateModel.setGeneratorRunning(true, 4500)
            └─ EnergySnapshot now shows generator=ON

Time 20s: Solar production increases to 2000W
         Production=2000W (solar) + 4500W (gen) = 6500W
         Consumption=1500W (coffee)
         Balance=-5000W (SURPLUS!)

         EnergyControlLoopTask.run():
         ├─ Calls handleSurplus():
         │  ├─ Stops generator: generatorop.stopGenerator()
         │  │  └─ Production now = 2000W
         │  ├─ Resume Fan: equipmentPorts["Fan-1"].resume()
         │  │  └─ Fan resumes to prior mode
         │  │  └─ Consumption: 1500 + 200 = 1700W
         │  ├─ Still surplus (2000 - 1700 = 300W)
         │  ├─ If batteries < 90%:
         │  │  └─ batteriesop.startCharging()
         │  │  └─ Batteries draw 300W
         │  │  └─ Production now consumed
         │  └─ Balanced again
         │
         └─ Updates state model:
            ├─ energyStateModel.setGeneratorRunning(false, 0)
            └─ energyStateModel.setBatteryState(level, true)
```

### 5.2 EnergyStateModel as Shared Truth

```
All Controllers Query EnergySnapshot:

Equipment:
  EnergySnapshot snap = coordinator.getEnergySnapshot();
  if (snap.balanceWatts < 0) {
      // Critical deficit: reduce power if possible
      setMode(ECO_MODE);
  } else if (snap.balanceWatts > 1000) {
      // Surplus available: can use full power
      setMode(MAX_MODE);
  }

Battery Controller:
  EnergySnapshot snap = coordinator.getEnergySnapshot();
  if (snap.balanceWatts > 500 && snap.batteryChargeLevel < 0.9) {
      // Good surplus and battery not full: charge
      startCharging();
  } else if (snap.batteryChargeLevel > 0.5 && snap.balanceWatts < 0) {
      // Deficit and batteries available: contribute discharge
      startDischarging();
  }

HEM Control Loop:
  EnergySnapshot snap = energyStateModel.getSnapshot();
  double deficit = snap.consumptionWatts - snap.productionWatts;
  if (deficit > 500) {
      // All controllers independently querying same snapshot
      // Make consistent decisions without mutual coupling
  }
```

---

## 6. XML Adapter Descriptors

Each appliance needs an XML descriptor (e.g., `adapters/coffeeci-descriptor.xml`):

```xml
<adapter>
    <offers interface="AdjustableCI" />
    <requires interface="" />
    <mode name="SUSPEND" consumption="3W" />
    <mode name="ECO" consumption="700W" />
    <mode name="NORMAL" consumption="1000W" />
    <mode name="MAX" consumption="1500W" />
    <method name="suspend" />
    <method name="resume" />
    <method name="currentMode" />
    <method name="getModeConsumption" />
</adapter>
```

---

## 7. Summary: HEM-ElectricMeter-Appliance Relationship

```
Energy Flow:
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│ SolarPanel   │         │  Generator   │         │  Batteries   │
└──────┬───────┘         └───────┬──────┘         └──────┬───────┘
       │ Power reading           │ Power reading         │ Power
       │ (0-5000W)               │ (0-4500W)             │ drain/charge
       │                         │                       │
       └────────┬────────────────┬───────────────────────┘
                │
                ↓
        ┌──────────────────────┐
        │  ElectricMeterCyPhy  │
        │                      │
        │ Aggregates:          │
        │ - Total production   │
        │ - Total consumption  │
        │ - Balance            │
        └──────────┬───────────┘
                   │
                   ↓
        ┌──────────────────────────────────┐
        │  HEMEnergyManager                 │
        │                                   │
        │ 1. Queries ElectricMeter:         │
        │    - getCurrentConsumption()      │
        │    - getCurrentProduction()       │
        │                                   │
        │ 2. Updates EnergyStateModel:      │
        │    - balanceWatts                 │
        │    - generatorRunning             │
        │    - batteriesCharging            │
        │                                   │
        │ 3. Makes decisions:               │
        │    - Suspend/resume appliances    │
        │    - Start/stop generator         │
        │    - Charge/discharge batteries   │
        │                                   │
        │ 4. Notifies controllers via       │
        │    EnergyStateModel sharing       │
        └──────┬───────────────┬────────────┘
               │               │
               ↓               ↓
        ┌────────────┐    ┌──────────────────────┐
        │ Appliances │    │ Batteries/Generator  │
        │            │    │                      │
        │ CoffeeMachine   │ Control via:         │
        │ Kettle          │ - batteriesop port   │
        │ Laundry         │ - generatorop port   │
        │ Fan             │                      │
        │                 │ State in:            │
        │ Register via:   │ EnergyStateModel     │
        │ RegistrationCI  │                      │
        │                 │                      │
        │ Control via:    │                      │
        │ AdjustableCI    │                      │
        │ ports (dynamic  │                      │
        │ connectors)     │                      │
        └────────────┘    └──────────────────────┘
```

---

## 8. Implementation Checklist for Etape 4

### Phase 1: HEMCyPhy Registration Infrastructure
- [ ] Add `@OfferedInterfaces(offered = { RegistrationCI.class })`
- [ ] Add `@RequiredInterfaces` for all energy sources
- [ ] Add RegistrationInboundPort fields for each appliance type
- [ ] Implement ports creation in `start()`
- [ ] Implement `register()` and `unregister()` methods with:
  - XML parsing
  - Dynamic connector generation (Javassist)
  - AdjustableOutboundPort creation and connection
  - Equipment registry tracking

### Phase 2: HEMEnergyManager Extension
- [ ] Create HEMEnergyManager extending HEMCyPhy
- [ ] Implement EnergyCoordinator interface
- [ ] Initialize EnergyStateModel and EquipmentRegistry
- [ ] Implement control loop scheduling in `execute()`
- [ ] Override `register()` to track equipment with priorities

### Phase 3: EnergyControlLoopTask
- [ ] Read ElectricMeter consumption/production periodically
- [ ] Update EnergyStateModel with readings
- [ ] Implement handleDeficit():
  - Suspend equipment by priority
  - Start generator if needed
  - Check battery discharge
- [ ] Implement handleSurplus():
  - Stop generator
  - Resume equipment by urgency
  - Charge batteries
- [ ] Update state model after each decision

### Phase 4: Appliance Registration
- [ ] Add RegistrationOutboundPort to CoffeeMachineCyPhy
- [ ] Add RegistrationOutboundPort to KettleCyPhy
- [ ] Add RegistrationOutboundPort to LaundryCyPhy
- [ ] Call `rop.register()` when ready (in turnOn() or initialise())
- [ ] Create XML descriptors with correct modes/consumption

### Phase 5: Testing & Validation
- [ ] Create comprehensive test scenario in CVMIntegrationTest
- [ ] Verify appliance registration
- [ ] Verify deficit detection and suspension
- [ ] Verify surplus detection and resumption
- [ ] Verify generator start/stop
- [ ] Verify battery charge/discharge
- [ ] Check EnergyStateModel consistency

---

## 9. Key Differences: Reference vs. Etape 4

| Feature | Reference HEMCyPhy | Etape 4 HEMEnergyManager |
|---------|-------------------|------------------------|
| **Appliance Control** | Hardcoded Heater (if isPreFirstStep) | Dynamic registration of Coffee/Kettle/Laundry |
| **Registration** | None | Full RegistrationCI implementation |
| **Control Loop** | Manual test scenarios only | Automatic periodic loop (EnergyControlLoopTask) |
| **Energy Coordination** | Direct meter queries | Via EnergyStateModel (data-centered) |
| **Decision Logic** | External (test scenario) | Internal (control loop with thresholds) |
| **Appliance Suspension** | Manual (via test) | Automatic by priority |
| **Generator Control** | Manual (via test) | Automatic by deficit threshold |
| **Battery Management** | Manual (via test) | Automatic charge/discharge |

