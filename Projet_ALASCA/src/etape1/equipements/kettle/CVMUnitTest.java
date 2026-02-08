package etape1.equipements.kettle;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.exceptions.BCMException;

// -----------------------------------------------------------------------------
/**
 * The class <code>CVMUnitTest</code> performs unit tests on the water heater
 * (chauffe-eau) component.
 *
 * <p>Created on : 2023-09-19</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class CVMUnitTest extends AbstractCVM {

	public CVMUnitTest() throws Exception {
		KettleUnitTester.VERBOSE = true;
		KettleUnitTester.X_RELATIVE_POSITION = 0;
		KettleUnitTester.Y_RELATIVE_POSITION = 0;
		Kettle.VERBOSE = true;
		Kettle.X_RELATIVE_POSITION = 1;
		Kettle.Y_RELATIVE_POSITION = 0;
	}

	@Override
	public void deploy() throws Exception {
		AbstractComponent.createComponent(
				Kettle.class.getCanonicalName(),
				new Object[]{ false });

		AbstractComponent.createComponent(
				KettleUnitTester.class.getCanonicalName(),
				new Object[]{ true });

		super.deploy();
	}

	public static void main(String[] args) {
		BCMException.VERBOSE = true;
		try {
			CVMUnitTest cvm = new CVMUnitTest();
			cvm.startStandardLifeCycle(10000L);
			Thread.sleep(10000L);
			System.exit(0);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
// -----------------------------------------------------------------------------
