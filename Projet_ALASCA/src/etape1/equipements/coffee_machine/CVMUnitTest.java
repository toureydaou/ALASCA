package etape1.equipements.coffee_machine;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.exceptions.BCMException;

public class CVMUnitTest extends AbstractCVM {
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public CVMUnitTest() throws Exception {
		CoffeeMachineUnitTester.VERBOSE = true;
		CoffeeMachineUnitTester.X_RELATIVE_POSITION = 0;
		CoffeeMachineUnitTester.Y_RELATIVE_POSITION = 0;
		CoffeeMachine.VERBOSE = true;
		CoffeeMachine.X_RELATIVE_POSITION = 1;
		CoffeeMachine.Y_RELATIVE_POSITION = 0;
	}

	// -------------------------------------------------------------------------
	// CVM life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.cvm.AbstractCVM#deploy()
	 */
	@Override
	public void deploy() throws Exception {
		AbstractComponent.createComponent(CoffeeMachine.class.getCanonicalName(), new Object[] {false});

		AbstractComponent.createComponent(CoffeeMachineUnitTester.class.getCanonicalName(), new Object[] { true });

		super.deploy();
	}

	public static void main(String[] args) {
		BCMException.VERBOSE = true;
		try {
			CVMUnitTest cvm = new CVMUnitTest();
			cvm.startStandardLifeCycle(30000L);
			Thread.sleep(20000L);
			System.exit(0);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
