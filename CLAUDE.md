# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ALASCA (A Laboratory for Active Smart-home Cyber-physical system Architectures) is an academic Java project implementing a Household Energy Management System (HEM) using **Sorbonne University's BCM4Java** (Basic Component Model) framework. The project demonstrates progressive software validation through three stages:

- **Etape 1**: Component-based architecture with unit testing
- **Etape 2**: Models-in-the-Loop (MIL) simulation using DEVS
- **Etape 3**: Software-in-the-Loop (SIL) and Cyber-Physical (CyPhy) integration testing
- **Etape 4**: Energy management control loop (boucle de contrôle de gestion d'énergie)

The working directory is `Projet_ALASCA/`. A reference implementation exists at `HEM-2025-etape3-02122025-src/`.

## Build and Test Commands

This is an Eclipse-based Java project with no build scripts. All compilation and execution is done through Eclipse or manual javac/java commands.

### Manual Compilation

```bash
cd Projet_ALASCA

# Compile all sources
javac -d bin -cp "src:BCM4Java-22012026.jar:BCM4Java-CyPhy-08012026.jar:NeoSim4Java-08012026.jar:commons-math3-3.6.1.jar:javassist.jar:jing.jar:CommonSunCalculations-07102025.jar" src/**/*.java

# After any code changes, ALWAYS do a clean rebuild
rm -rf bin/*
# Then recompile as above
```

### Running Tests

**Etape 1 - Unit Tests (Component-based)**
```bash
# Run via Eclipse or:
java -cp "bin:BCM4Java-22012026.jar:..." etape1.equipements.coffee_machine.CVMUnitTest
java -cp "bin:BCM4Java-22012026.jar:..." etape1.equipements.laundry.CVMUnitTest
java -cp "bin:BCM4Java-22012026.jar:..." etape1.equipements.fan.CVMUnitTest
```

**Etape 2 - MIL Simulation**
```bash
java -cp "bin:BCM4Java-22012026.jar:NeoSim4Java-08012026.jar:commons-math3-3.6.1.jar:..." etape2.RunGlobalSimulation
```

**Etape 3 - Integration Tests**
```bash
# Standard integration test (no simulation)
java -cp "bin:BCM4Java-22012026.jar:BCM4Java-CyPhy-08012026.jar:NeoSim4Java-08012026.jar:..." etape3.CVMIntegrationTest

# SIL integration test (with real-time simulation)
java -cp "bin:BCM4Java-22012026.jar:BCM4Java-CyPhy-08012026.jar:NeoSim4Java-08012026.jar:..." etape3.CVMSILIntegrationTest
```

**Etape 4 - Energy Management Control Loop**
```bash
# Integration test (without simulation)
java -cp "bin:BCM4Java-22012026.jar:BCM4Java-CyPhy-08012026.jar:NeoSim4Java-08012026.jar:..." etape4.CVMIntegrationTest
```

**CRITICAL**: Always clean and rebuild after modifying source files. `.class` files are not automatically regenerated.

## Architecture Patterns

### Component Structure (BCM4Java)

All components extend `AbstractComponent` from `fr.sorbonne_u.components` and follow a strict port-connector pattern:

```
Component/
├── [Component].java              (main component class)
├── interfaces/
│   ├── [Component]UserI.java     (user-facing operations)
│   ├── [Component]InternalControlI.java  (state management)
│   ├── [Component]ExternalControlI.java  (adjustable modes)
│   ├── [Component]CI.java        (component interface)
│   └── [Component]ImplementationI.java   (enums: State, Mode)
├── ports/ or connections/ports/
│   ├── [Component]UserInboundPort.java
│   ├── [Component]UserOutboundPort.java
│   ├── [Component]InternalInboundPort.java
│   ├── [Component]ExternalControlInboundPort.java
│   └── ...OutboundPort.java variants
└── connectors/ or connections/connectors/
    ├── [Component]UserConnector.java
    └── [Component]ExternalControlConnector.java
```

**Key patterns:**
- Inbound ports receive calls INTO the component
- Outbound ports send calls OUT OF the component
- Connectors mediate between inbound/outbound ports
- XML adapter descriptors (`adapters/`) enable dynamic connector generation

### Adjustable Equipment Pattern

Equipment implementing `AdjustableCI` (in `etape1/bases/`) must support:
- `maxMode()`, `currentMode()`: Query operating modes
- `upMode()`, `downMode()`: Navigate between modes
- `suspend()`, `resume()`: Reduce/restore power consumption
- `getModeConsumption(int)`: Query power per mode

Modes are equipment-specific (e.g., CoffeeMachine: SUSPEND/ECO/NORMAL/MAX, Fan: LOW/MEDIUM/HIGH).

### Registration Pattern (Etape 3)

In integration tests, equipment registers with HEM dynamically:

1. Equipment creates `RegistrationOutboundPort` in constructor
2. Equipment connects port to HEM during `start()` lifecycle
3. Equipment calls `rop.register(uid, portURI, xmlDescriptor)` when ready
4. HEM parses XML, generates connector using Javassist, stores in `equipmentPorts` map

**CRITICAL**: Always initialize `this.uid` field in component's `initialise()` method. Registration uses `uid` as the map key.

### DEVS Simulation (Etape 2)

Uses HIOA (Hybrid Input/Output Automata) models from `fr.sorbonne_u.devs_simulation`:

**Model Types:**
- **Atomic Models**: Individual simulation components (e.g., `CoffeeMachineElectricityModel`)
- **Coupled Models**: Composition of atomic models with bindings
- **HIOA Models**: Support continuous variables (derivatives) + discrete events

**Key Classes:**
- `AtomicHIOA_Descriptor`: Describes atomic models with imported/exported events and variables
- `CoupledHIOA_Descriptor`: Describes coupled models with submodels and bindings
- `EventSource`/`EventSink`: Route discrete events between models
- `VariableSource`/`VariableSink`: Bind continuous variables between models

**Simulation Structure (per equipment):**
- Electricity model (power consumption based on mode)
- Optional temperature/state models
- Tester model (generates test events)

Global simulation orchestrated in `RunGlobalSimulation.java`:
- Atomic/Coupled model descriptors
- Event connections (e.g., `SwitchOnCoffeeMachine` → multiple models)
- Variable bindings (e.g., `currentHeatingPower`: Electricity → Temperature)

### SIL Architecture (Etape 3)

**Three Test Modes:**

1. **Standard Unit Tests** (`CVMStandardUnitTest`): Direct method calls, no simulation
2. **Unit Tests with SIL** (`CVMUnitTestWithSILSimulation`): Component + embedded SIL simulator
3. **CyPhy Integration Tests** (`CVMIntegrationTest`, `CVMSILIntegrationTest`): Real-time cyber-physical testing

**SIL Models:**
- Located in `sil/` subdirectories (e.g., `etape3/equipements/coffee_machine/sil/`)
- Extend MIL models but run in real-time
- Use `RTAtomicSimulatorPlugin` for real-time execution
- Synchronized via `AcceleratedClock` and `ClocksServer`

**Local Simulation Architectures:**
Each component has a `Local_SIL_SimulationArchitectures.java` with TWO methods:
- `create*SIL_Architecture4UnitTest()`: Full local architecture with all models
- `create*SIL_Architecture4IntegrationTest()`: Minimal architecture, electricity models moved to ElectricMeter

**Integration Test Model Distribution:**
```
FanCyPhy:
  └── FanStateSILModel (exports events → ElectricMeter)

CoffeeMachineCyPhy:
  └── CoffeeMachineCoupledModel
      ├── CoffeeMachineStateSILModel (exports events)
      └── CoffeeMachineTemperatureSILModel (imports vars - MAY BE NULL!)

ElectricMeterCyPhy:
  └── ElectricMeterCoupledModel
      ├── ElectricMeterElectricitySILModel (imports intensities)
      ├── FanElectricitySILModel (exports currentIntensity)
      └── CoffeeMachineElectricitySILModel (exports currentIntensity, currentHeatingPower, currentWaterLevel)
```

**Variable Bindings** (defined in `LocalSimulationArchitectures.createElectricMeterSILArchitecture()`):
- `FanElectricitySILModel.currentIntensity` → `ElectricMeterElectricitySILModel.currentFanIntensity`
- `CoffeeMachineElectricitySILModel.currentIntensity` → `ElectricMeterElectricitySILModel.currentCoffeeMachineIntensity`

**Global Architecture** (defined in `ComponentSimulationArchitectures.java`):
- Declares component models as atomic model descriptors
- Defines event connections between components
- Does NOT define cross-component variable bindings (handled locally)

**CRITICAL for SIL models:**
- Always verify imported variables are initialized in `fixpointInitialiseVariables()` before calling `computeDerivatives()`
- Return `new Pair<>(0, 1)` if imported variables not ready (forces another iteration)
- Never access `.getValue()` on uninitialized variables (causes NullPointerException)
- In integration test mode, imported variables may be `null` if they're in another component - always provide fallback values

### Energy Management Control Loop (Etape 4)

**Objective**: Implement an intelligent control loop in the HEM that periodically monitors energy production vs consumption and dynamically adjusts equipment to maintain balance.

**Core Principle**: Production >= Consumption at all times. When deficit occurs, suspend low-priority equipment. When surplus returns, resume suspended equipment.

**Architecture**:
```
etape4/
├── CVMIntegrationTest.java              # Integration test harness
├── control/
│   ├── PriorityConfig.java              # Static priority levels per equipment type
│   └── EquipmentConstraint.java         # Interface for suspension constraints
└── equipements/hem/
    ├── HEMEnergyManager.java            # Extends HEMCyPhy with control loop
    ├── EquipmentRegistry.java           # Thread-safe equipment tracking
    └── EnergyControlLoopTask.java       # Periodic control algorithm (AbstractTask)
```

**Key Components**:

- **HEMEnergyManager** (`extends HEMCyPhy`): Main component. Overrides `register()` to track equipment in `EquipmentRegistry` with priorities. Schedules `EnergyControlLoopTask` at fixed rate during `execute()`.
- **EquipmentRegistry**: Thread-safe (ReentrantLock) registry of `EquipmentInfo` objects. Provides `getSuspendableEquipment()` (sorted by priority, low importance first) and `getSuspendedEquipment()`.
- **EnergyControlLoopTask**: Runs periodically. Reads meter, estimates consumption from equipment states if meter returns 0 (INTEGRATION_TEST mode), detects deficit/surplus, suspends/resumes equipment via `AdjustableCI` ports.
- **PriorityConfig**: Static priorities (1=highest/critical to 10=lowest). Default: Generator=1, Batteries=2, Laundry=4, CoffeeMachine=6, Fan=7.

**Control Algorithm** (in `EnergyControlLoopTask.run()`):
1. Read consumption/production from ElectricMeter
2. If both are 0 (no SIL simulation), estimate consumption by querying each equipment's `currentMode()` and `getModeConsumption(mode)` via AdjustableCI port
3. Calculate balance = consumption - production
4. If balance > 0.5A (deficit): suspend equipment by priority (lowest importance first)
5. If balance < -0.5A (surplus): resume suspended equipment by urgency (`emergency()`)

**Implementation Phases**:
- Phase 1 (current): Single equipment (CoffeeMachine), basic deficit detection and suspension
- Phase 2: Multiple equipment types with priority-based suspension order
- Phase 3: Multiple instances of same equipment type
- Phase 4: Generator start/stop and battery charge/discharge
- Phase 5: Advanced scenarios (time-of-day, weather-based)

**Known Issues & Lessons Learned**:

1. **UID from registration**: `CoffeeMachineCyPhy.uid` is set to `COFFEE_MACHINE_CONNECTOR_NAME = "CoffeeMachineGeneratedConnector"` (line 250 of CoffeeMachineCyPhy.java), NOT a semantic UID like `"CoffeeMachine-1"`. The `extractType()` method in HEMEnergyManager must handle this format.

2. **ElectricMeter returns 0 in INTEGRATION_TEST mode**: Without SIL simulation, `getCurrentConsumption()` and `getCurrentProduction()` always return 0.0 (hardcoded in ElectricMeterCyPhy lines 1107-1114). The control loop must estimate consumption by querying equipment directly.

3. **Coffee Machine mode after turnOn()**: `turnOn()` sets `currentMode = SUSPEND` (3W). The XML connector initializes `currentMode = 1`. Mode 1 = 3W (~0.014A), which is below the action threshold (0.5A). To create a visible deficit, the test scenario must also call `setCurrentPowerLevel(1500W)` or `upMode()`.

4. **XML adapter consumption values** (from `coffeeci-descriptor.xml`):
   - Mode 1 (SUSPEND): 3W
   - Mode 2 (ECO): 700W
   - Mode 3 (NORMAL): 1000W
   - Mode 4 (MAX): 1500W

5. **BCM4Java logging**: `logMessage()` and `traceMessage()` go to BCM4Java tracer windows (GUI), NOT to stdout. Use `System.out.println()` for console-visible logs, or use the `log()` helper methods in HEMEnergyManager/EnergyControlLoopTask.

6. **HEMCyPhy fields visibility**: `equipementsRegitered` and `equipmentPorts` in HEMCyPhy are `protected` (modified from `private`) to allow HEMEnergyManager subclass access.

7. **Compilation**: FanTester.java and FanTesterCyPhy.java have JUnit dependency errors (`org.junit.jupiter.api`). They are transitively compiled even when excluded from source list because other files reference them. Compile via Eclipse or exclude all files that import them.

## Equipment Domain Knowledge

### Coffee Machine
- States: OFF, ON, HEATING
- Modes: SUSPEND (0W), ECO (500W), NORMAL (1000W), MAX (1500W)
- Temperature range: 20-95°C, Water capacity: 1.0L
- MIL models (etape2): CoffeeMachineElectricityModel + CoffeeMachineTemperatureModel
- SIL models (etape3): CoffeeMachineStateSILModel, CoffeeMachineElectricitySILModel, CoffeeMachineTemperatureSILModel
- Etape 3 status: COMPLETE (CyPhy component, controller, tester, all SIL models, unit tests)

### Laundry Machine
- States: OFF, STANDBY, WASHING
- Wash modes: DELICATE, COLOR, WHITE, INTENSIVE
- Spin speeds: 800, 1000, 1200 RPM, Temperature range: 30-90°C
- MIL models (etape2): LaundryElectricityModel (no temperature model)
- SIL models (etape3): LaundryStateSILModel, LaundryElectricitySILModel
- Etape 3 status: COMPLETE (CyPhy component, controller, tester, all SIL models, unit tests, integration tests)
- Operations: `startWash()`, `cancelWash()`

### Fan
- States: OFF, ON
- Modes: LOW, MEDIUM, HIGH
- Power: LOW < MEDIUM < HIGH
- MIL models (etape2): FanElectricityModel
- SIL models (etape3): FanStateSILModel, FanElectricitySILModel
- Etape 3 status: COMPLETE (CyPhy component, tester, all SIL models, unit tests)
- **Mode transition constraints**:
  - `setHigh()` requires current mode = LOW
  - `setLow()` requires current mode = HIGH

### Kettle (Water Heater / Chauffe-eau)
- States: OFF, ON, HEATING (KettleImplementationI.KettleState)
- Modes: SUSPEND (0W), ECO (1000W), NORMAL (2000W), MAX (3000W)
- Tank: 200L fixed, Voltage: 220V, Temperature target: 55°C, range 30-80°C
- MIL models (etape2): KettleElectricityModel + KettleTemperatureModel
- SIL models (etape3): KettleStateSILModel, KettleElectricitySILModel, KettleTemperatureSILModel
- Etape 3 status: COMPLETE (CyPhy component, controller, tester, all SIL models, unit tests, integration tests)

### Electric Meter
- Aggregates power consumption from all equipment
- Tracks production from batteries/solar panels
- MIL model (etape2): ElectricMeterElectricityModel (imports currentFanIntensity, currentCoffeeMachineIntensity, currentLaundryIntensity, currentKettleIntensity)
- SIL model (etape3): ElectricMeterElectricitySILModel (in ElectricMeterCoupledModel, imports equipment electricity SIL models)
- Etape 3 status: COMPLETE (includes Laundry and Kettle SIL models)

### Generator, Solar Panel, Batteries
- Defined in Etape 1 & 2, not fully integrated in Etape 3
- Generator: Fuel consumption, max 4500W
- Solar panel: Time-dependent production with sunrise/sunset models
- Batteries: Charge/discharge cycles, configurable capacity

## Common Pitfalls

1. **Forgetting to initialize `this.uid`**: Causes registration failures in Etape 3 integration tests. Always set in `initialise()`.

2. **Port connection timing**: Ports must be published before connection. Connection happens in `start()` lifecycle, after constructors.

3. **Mode transition preconditions**: Equipment like Fan has strict state machine rules. Violating preconditions causes assertion failures.

4. **Simulation architecture structure**: In SIL, some models are internal to coupled models (e.g., `CoffeeMachineTemperatureSILModel` is inside `CoffeeMachineCoupledModel`, NOT a global submodel). Check reference implementation in `HEM-2025-etape3-02122025-src/`.

5. **Variable initialization order**: In SIL models, always verify imported variables are ready before computing derivatives. Use the pattern from `HeaterTemperatureSILModel` reference.

6. **Duplicate connections**: When defining event connections in `ComponentSimulationArchitectures`, avoid duplicating connections. Each event route should be defined once.

7. **Clean rebuild requirement**: Java `.class` files are not auto-regenerated. Always clean build after code changes, especially when debugging.

8. **Package import inconsistency (`physical_data`)**: The project has TWO versions of measurement classes:
   - Local: `physical_data.MeasurementUnit`, `physical_data.Measure`
   - Framework: `fr.sorbonne_u.alasca.physical_data.MeasurementUnit`, `fr.sorbonne_u.alasca.physical_data.Measure`

   **CRITICAL**: Always use `fr.sorbonne_u.alasca.physical_data.*` for consistency. Mixing packages causes invariant check failures because enum comparisons between different classes always return `false`.

9. **Fixpoint initialization protocol**: HIOA models that export variables MUST implement:
   ```java
   @Override
   public boolean useFixpointInitialiseVariables() { return true; }

   @Override
   public Pair<Integer, Integer> fixpointInitialiseVariables() {
       if (!this.exportedVar.isInitialised()) {
           this.exportedVar.initialise(defaultValue);
           return new Pair<>(1, 0);  // 1 initialized, 0 pending
       }
       return new Pair<>(0, 0);  // Already done
   }
   ```
   Without this, the fixpoint iteration fails with "circular dependencies or errors in the implementation".

10. **Cross-component variable bindings in integration tests**: In SIL integration tests, models are distributed across components:
    - `FanStateSILModel` → in `FanCyPhy`
    - `FanElectricitySILModel` → in `ElectricMeterCyPhy` (not FanCyPhy!)
    - `CoffeeMachineElectricitySILModel` → in `ElectricMeterCyPhy`
    - `CoffeeMachineTemperatureSILModel` → in `CoffeeMachineCyPhy`

    **Problem**: If a model imports variables from another component (e.g., Temperature imports from Electricity), but they're in different components, the variable binding may not exist.

    **Solution**: Models must handle `null` imported variables gracefully:
    ```java
    double value = (this.importedVar != null && this.importedVar.isInitialised())
        ? this.importedVar.getValue()
        : DEFAULT_VALUE;  // Fallback for integration test mode
    ```

11. **SIL temperature simulation and controller integration**: For proper temperature control in SIL integration tests:
    - **Temperature formula**: Match etape2 formula:
      - `computeDerivatives()`: returns °C/s (no multiplication factor)
      - `computeNewTemperature()`: `newTemp = oldTemp + derivative * deltaT * 3600` (converts hours to seconds)
    - **Heating control flow**:
      1. Test scenario calls `turnOn()` → state = ON, controller starts monitoring
      2. Test scenario calls `startHeating()` → state = HEATING, temperature model starts heating
      3. Controller monitors temperature and calls `stopHeating()` when target reached
    - **Critical**: `startHeating()` must send state notification to controller via `sensorInboundPort.send()`
    - **Physical parameters**: With 1500W power, 1L water heats at ~21.5°C/min (reaches 100°C in ~4 min)

## File Organization

```
Projet_ALASCA/
├── src/
│   ├── etape1/
│   │   ├── bases/                   # AdjustableCI, RegistrationCI, ConnectorGenerator
│   │   ├── equipements/             # French naming: coffee_machine, laundry, fan, hem, kettle
│   │   └── equipments/              # English naming: meter, batteries, generator, solar_panel
│   ├── etape2/
│   │   ├── RunGlobalSimulation.java
│   │   ├── GlobalCoupledModel.java
│   │   └── equipments/*/mil/        # MIL simulation models
│   ├── etape3/
│   │   ├── CVMIntegrationTest.java
│   │   ├── CVMSILIntegrationTest.java
│   │   ├── ComponentSimulationArchitectures.java
│   │   ├── GlobalSupervisor.java
│   │   ├── CoordinatorComponent.java
│   │   └── equipements/*/           # CyPhy components + sil/ subdirectories
│   ├── etape4/
│   │   ├── CVMIntegrationTest.java          # Integration test harness
│   │   ├── control/                         # PriorityConfig, EquipmentConstraint
│   │   └── equipements/hem/                 # HEMEnergyManager, EquipmentRegistry, EnergyControlLoopTask
│   ├── tests_utils/                 # Test framework (TestScenario, TestStep, etc.)
│   └── physical_data/               # Measure<T>, MeasurementUnit, SignalData<T>
├── adapters/                        # XML descriptor files for connector generation
├── bin/                             # Compiled .class files
└── *.jar                            # BCM4Java, NeoSim4Java, commons-math3, etc.
```

**Note**: Inconsistent naming (French `equipements` vs English `equipments`) exists in codebase. Pay attention when navigating.

## Reference Implementation

The `HEM-2025-etape3-02122025-src/` directory contains a reference implementation with Heater equipment. When implementing similar features:
- Check `HeaterTemperatureSILModel.java` for SIL model patterns
- Check `ComponentSimulationArchitectures.java` for simulation architecture structure
- Check `HEMCyPhy.java` for registration and equipment management patterns

## Testing Guidelines

**Etape 1 (Unit Tests):**
- Use `TestScenario` framework from `tests_utils/`
- Define test steps with Gherkin-style specifications
- Track statistics with `TestsStatistics`

**Etape 2 (MIL Simulation):**
- Define scenario in `RunGlobalSimulation.java`
- Use `SimulationTestStep` for timed events
- Verify model outputs in `GlobalReport`

**Etape 3 (Integration/SIL):**
- Two execution modes: `INTEGRATION_TEST` and `INTEGRATION_TEST_WITH_SIL_SIMULATION`
- Use `ClocksServer` for time synchronization
- Test scenarios schedule events at specific simulated times
- Verify component interactions through HEM
- For SIL: Verify real-time simulation models match physical behavior

**Etape 4 (Energy Control Loop):**
- Uses `INTEGRATION_TEST` mode (no SIL simulation needed for control loop logic)
- `ClocksServer` for time synchronization, `ACCELERATION_FACTOR = 1.0` (real-time)
- Control loop period: `CONTROL_PERIOD_SECONDS = 10.0` (configurable in HEMEnergyManager)
- Test scenario turns on equipment, sets power level, then verifies control loop reacts
- Logs via `System.out.println()` (prefixed `[HEM]` for HEMEnergyManager, `[CONTROL LOOP]` for task)
- Equipment registers during `turnOn()` in CoffeeMachineCyPhy (not at component start)

## Debug Workflow

When a test fails:

1. Add `traceMessage()` or `logMessage()` calls in component methods
2. Clean rebuild: `rm -rf bin/* && javac ...`
3. Re-run test and check console output
4. For registration issues: Verify `uid` initialization, port publication timing
5. For simulation issues: Check variable initialization order, event routing
6. Compare with reference implementation in `HEM-2025-etape3-02122025-src/`

### Common SIL Integration Test Errors

**"Static invariant violation in class ... MeasurementUnit.AMPERES.equals(...)"**
- **Cause**: Mixed imports between `physical_data.*` and `fr.sorbonne_u.alasca.physical_data.*`
- **Fix**: Use `fr.sorbonne_u.alasca.physical_data.*` everywhere

**"fixpoint initialisation protocol error (circular dependencies or errors in the implementation)"**
- **Cause 1**: Model doesn't implement `useFixpointInitialiseVariables()` returning `true`
- **Cause 2**: Model waits for imported variables that are never bound (cross-component)
- **Cause 3**: Model returns `notInitialisedYet > 0` indefinitely
- **Fix**: Implement proper fixpoint methods, handle null imported variables with fallback values

**"elapsed time X is larger than the next time advance 0.0"**
- **Cause**: Usually happens AFTER another initialization error breaks the simulation
- **Fix**: Fix the root cause (invariant violation or fixpoint error) first

**NullPointerException in `computeDerivatives()` or similar**
- **Cause**: Accessing `.getValue()` on imported variable that is `null` in integration test mode
- **Fix**: Add null checks with fallback values:
  ```java
  double value = (this.importedVar != null && this.importedVar.isInitialised())
      ? this.importedVar.getValue() : DEFAULT_VALUE;
  ```

**Temperature stays constant (e.g., 20°C) in SIL integration test**
- **Cause 1**: Model heats only when `state == HEATING`, but test scenario only calls `turnOn()` (not `startHeating()`)
- **Cause 2**: Missing acceleration factor - etape2 MIL uses `* 3600` in `computeNewTemperature()`
- **Fix**:
  1. Add `startHeating()` call in test scenario after `turnOn()`:
     ```java
     ((CoffeeMachineTesterCyPhy)owner).getCmInternalOP().startHeating();
     ```
  2. Match etape2 formula: `computeDerivatives()` returns °C/s (no multiplier), `computeNewTemperature()` uses `* 3600`:
     ```java
     // computeDerivatives: derivative in °C/s
     heatingContribution = power / (waterMass * WATER_SPECIFIC_HEAT_CAPACITY);
     // computeNewTemperature: convert hours to seconds
     newTemp = oldTemp + derivative * deltaT * 3600;
     ```
  3. Ensure `startHeating()` sends state notification to controller:
     ```java
     this.sensorInboundPort.send(new CoffeeMachineStateSensorData(this.currentState));
     ```

**Controller doesn't stop heating at target temperature**
- **Cause**: Controller monitors `priorState == HEATING`, but wasn't notified of state change
- **Fix**: Ensure `CoffeeMachineCyPhy.startHeating()` sends state notification via `sensorInboundPort.send()`

## Étape 3 Implementation Guide

### Current Implementation Status (Étape 3)

| Component | CyPhy | Controller | Tester | SIL Models | Local Arch | Unit Tests | Integration |
|-----------|-------|------------|--------|------------|------------|------------|-------------|
| CoffeeMachine | ✅ | ✅ | ✅ | ✅ (State+Elec+Temp) | ✅ | ✅ | ✅ |
| Fan | ✅ | N/A | ✅ | ✅ (State+Elec) | ✅ | ✅ | ✅ |
| Laundry | ✅ | ✅ | ✅ | ✅ (State+Elec) | ✅ | ✅ | ✅ |
| Kettle | ✅ | ✅ | ✅ | ✅ (State+Elec+Temp) | ✅ | ✅ | ✅ |
| ElectricMeter | ✅ | N/A | N/A | ✅ | ✅ | N/A | ✅ |
| HEM | ✅ | N/A | N/A | N/A | N/A | N/A | ✅ |

### SIL Model Architecture Patterns

#### StateSILModel Pattern (from FanStateSILModel / CoffeeMachineStateSILModel)
- Extends `AtomicModel` (NOT HIOA - no continuous variables)
- Imports ALL events from component AND re-exports them to ElectricityModel
- `@ModelExternalEvents(imported = {...}, exported = {...})` — same event set
- Tracks `currentState` and `currentMode` internally
- In `userDefinedExternalTransition()`: receives event, updates state, re-emits to other models
- Accesses component via `AtomicSimulatorPlugin.getOwner()` to read/write state
- SIL-specific: events execute on component via `((ComponentCyPhy)owner).setMode()` etc.

#### ElectricitySILModel Pattern (from FanElectricitySILModel / CoffeeMachineElectricitySILModel)
- Extends the MIL ElectricityModel (e.g., `CoffeeMachineElectricityModel`)
- Overrides `setSimulationRunParameters()` to get owner component reference
- Implements `SIL_OperationI` (extends MIL `OperationI`)
- Key difference from MIL: when events execute, they also update the component state
- In integration tests: lives inside `ElectricMeterCoupledModel`, NOT in equipment component

#### TemperatureSILModel Pattern (from CoffeeMachineTemperatureSILModel)
- Extends the MIL TemperatureModel
- Must handle `null` imported variables (when electricity model is in another component)
- Overrides `computeDerivatives()` and `computeNewTemperature()` with null-safety
- In integration tests: lives inside equipment component's CoupledModel

#### SIL_OperationI Pattern
- Interface extending MIL OperationI (e.g., `SIL_CoffeeMachineOperationI extends CoffeeMachineOperationI`)
- Adds methods like `setCurrentHeatingPower(double, Time)`, `setCurrentWaterLevel(double, Time)`
- Implemented by ElectricitySILModel

### Local_SIL_SimulationArchitectures Pattern
Each equipment has TWO architecture methods:

1. **`create*SIL_Architecture4UnitTest()`**: Full local architecture
   - All models (State + Electricity + Temperature) in ONE component
   - All event connections and variable bindings are local
   - Used by `CVMUnitTestWithSILSimulation`

2. **`create*SIL_Architecture4IntegrationTest()`**: Minimal architecture
   - Only State model (+ Temperature if applicable) in equipment component
   - Electricity model moved to ElectricMeter component
   - State model exports events globally (cross-component)
   - Used by `CVMIntegrationTest` / `CVMSILIntegrationTest`

### Integration Test Model Distribution (Target for 4 components)
```
FanCyPhy:
  └── FanStateSILModel (exports events → ElectricMeter)

CoffeeMachineCyPhy:
  └── CoffeeMachineCoupledModel
      ├── CoffeeMachineStateSILModel (exports events → ElectricMeter)
      └── CoffeeMachineTemperatureSILModel (imports vars, null-safe)

LaundryCyPhy:
  └── LaundryStateSILModel (exports events → ElectricMeter)

KettleCyPhy:
  └── KettleCoupledModel
      ├── KettleStateSILModel (exports events → ElectricMeter)
      └── KettleTemperatureSILModel (imports vars, null-safe)

ElectricMeterCyPhy:
  └── ElectricMeterCoupledModel
      ├── ElectricMeterElectricitySILModel (imports all intensities)
      ├── FanElectricitySILModel
      ├── CoffeeMachineElectricitySILModel
      ├── LaundryElectricitySILModel
      └── KettleElectricitySILModel
```

### ComponentSimulationArchitectures.java
- Defines global simulation architecture across all components
- Declares each component's local model as an atomic model descriptor
- Defines cross-component event connections (State → Electricity in ElectricMeter)
- Does NOT define variable bindings (those are local to each component)
- Includes event routes for all four equipment types (Fan, CoffeeMachine, Laundry, Kettle)

### Files Created/Modified for Each Equipment (Laundry, Kettle) — COMPLETED

**New files per equipment (following CoffeeMachine/Fan pattern):**
```
etape3/equipements/<equipment>/
├── <Equipment>CyPhy.java                 # Main CyPhy component (extends etape1 component)
├── <Equipment>TesterCyPhy.java           # Tester component
├── <Equipment>Controller.java            # Controller (only for temp-controlled: Kettle)
├── CVMStandardUnitTest.java              # Unit test without simulation
├── CVMUnitTest.java                      # Unit test with component interaction
├── CVMUnitTestWithSILSimulation.java     # Unit test with SIL simulation
├── <Equipment>ActuatorCI.java            # Actuator interface
├── <Equipment>SensorDataCI.java          # Sensor data interface
├── <Equipment>PushImplementationI.java   # Push implementation interface
├── connections/
│   ├── connectors/<Equipment>ActuatorConnector.java
│   ├── connectors/<Equipment>SensorDataConnector.java
│   ├── ports/<Equipment>ActuatorInboundPort.java
│   ├── ports/<Equipment>ActuatorOutboundPort.java
│   ├── ports/<Equipment>SensorDataInboundPort.java
│   └── ports/<Equipment>SensorDataOutboundPort.java
├── sensor_data/
│   ├── <Equipment>SensorDataI.java
│   ├── <Equipment>StateSensorData.java
│   └── ... (equipment-specific sensor data classes)
└── sil/
    ├── <Equipment>StateSILModel.java
    ├── <Equipment>ElectricitySILModel.java
    ├── <Equipment>TemperatureSILModel.java  (if applicable)
    ├── SIL_<Equipment>OperationI.java
    ├── Local_SIL_SimulationArchitectures.java
    └── Run<Equipment>UnitarySILSimulation.java
```

**Files to modify:**
- `etape3/ComponentSimulationArchitectures.java` — add event routes
- `etape3/equipements/meter/sil/LocalSimulationArchitectures.java` — add electricity SIL model + bindings
- `etape3/CVMIntegrationTest.java` — add component creation
- `etape3/CVMSILIntegrationTest.java` — add component creation

## Integration Test Analysis

See `Projet_ALASCA/INTEGRATION_TEST_ANALYSIS.md` for detailed troubleshooting of Etape 3 integration tests, including:
- Component lifecycle phases (creation → start → execution)
- Registration flow between equipment and HEM
- Common errors (NullPointerException, port connection failures)
- Solutions applied (uid initialization, port connection, variable guards)
