package etape4.control;

/**
 * The interface <code>EquipmentConstraint</code> defines the contract for
 * checking whether equipment can be suspended.
 *
 * <p>
 * Different equipment types have different operational constraints. For
 * example, a laundry machine cannot be suspended mid-wash, but a coffee machine
 * can always be suspended.
 * </p>
 *
 * @author Jacques Malenfant, Sorbonne Universite
 */
public interface EquipmentConstraint {

	/**
	 * Check if suspension is currently allowed.
	 *
	 * @return true if equipment can be suspended now
	 */
	boolean allowsSuspension();

	/**
	 * Check if an operation is currently in progress.
	 *
	 * @return true if equipment is mid-operation
	 */
	boolean isOperationInProgress();

	/**
	 * Get reason why suspension is not allowed (if applicable).
	 *
	 * @return reason string, or empty if suspension allowed
	 */
	String getReason();

	// -------------------------------------------------------------------------
	// Default Implementations
	// -------------------------------------------------------------------------

	/**
	 * Default implementation that always allows suspension.
	 */
	public static class AlwaysSuspendable implements EquipmentConstraint {
		@Override
		public boolean allowsSuspension() {
			return true;
		}

		@Override
		public boolean isOperationInProgress() {
			return false;
		}

		@Override
		public String getReason() {
			return "";
		}
	}

	/**
	 * Default implementation that never allows suspension.
	 */
	public static class NeverSuspendable implements EquipmentConstraint {
		private final String reason;

		public NeverSuspendable(String reason) {
			this.reason = reason;
		}

		@Override
		public boolean allowsSuspension() {
			return false;
		}

		@Override
		public boolean isOperationInProgress() {
			return false;
		}

		@Override
		public String getReason() {
			return reason;
		}
	}
}
