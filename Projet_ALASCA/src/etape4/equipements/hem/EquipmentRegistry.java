package etape4.equipements.hem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import etape1.equipements.hem.ports.AdjustableOutboundPort;
import etape4.control.EquipmentConstraint;

/**
 * The class <code>EquipmentRegistry</code> manages tracking of all equipment
 * registered with the HEM for energy management purposes.
 *
 * <p>
 * This registry maintains equipment state, priorities, and constraints to
 * support intelligent control decisions.
 * </p>
 *
 * <p>
 * Thread-safe via ReentrantLock for collection operations.
 * </p>
 *
 * @author Jacques Malenfant, Sorbonne Universite
 */
public class EquipmentRegistry {

	// -------------------------------------------------------------------------
	// Inner Classes
	// -------------------------------------------------------------------------

	/**
	 * Information about a registered equipment for energy management.
	 */
	public static class EquipmentInfo {
		/** Unique identifier for this equipment (e.g., "CoffeeMachine-1") */
		public final String uid;

		/** Port to control this equipment via AdjustableCI */
		public final AdjustableOutboundPort port;

		/** Priority level: 1 (highest) to 10 (lowest) */
		public int priority;

		/** Current operating mode index */
		public int currentMode;

		/** Whether this equipment is currently suspended */
		public boolean suspended;

		/** Mode before suspension (for restoration) */
		public int priorSuspendMode;

		/** Constraint checker for this equipment */
		public final EquipmentConstraint constraint;

		/**
		 * Constructor for EquipmentInfo.
		 *
		 * @param uid        unique identifier
		 * @param port       outbound port for control
		 * @param priority   priority level (1-10)
		 * @param constraint constraint checker
		 */
		public EquipmentInfo(
			String uid,
			AdjustableOutboundPort port,
			int priority,
			EquipmentConstraint constraint
		) {
			this.uid = uid;
			this.port = port;
			this.priority = priority;
			this.constraint = constraint;
			this.suspended = false;
			this.currentMode = 0;
			this.priorSuspendMode = 0;
		}

		/**
		 * Check if this equipment can currently be suspended.
		 *
		 * @return true if suspension is allowed
		 */
		public boolean canSuspend() {
			return !suspended && constraint.allowsSuspension();
		}

		/**
		 * Get power consumption for a specific mode.
		 *
		 * @param mode mode index
		 * @return power consumption in Watts
		 * @throws Exception if port call fails
		 */
		public double getModeConsumption(int mode) throws Exception {
			return port.getModeConsumption(mode);
		}

		/**
		 * Get emergency/urgency level for resumption (0.0 to 1.0).
		 *
		 * @return urgency value
		 * @throws Exception if port call fails
		 */
		public double emergency() throws Exception {
			return suspended ? port.emergency() : 0.0;
		}

		@Override
		public String toString() {
			return String.format("EquipmentInfo[uid=%s, priority=%d, mode=%d, suspended=%s]",
				uid, priority, currentMode, suspended);
		}
	}

	// -------------------------------------------------------------------------
	// Constants and Variables
	// -------------------------------------------------------------------------

	/** Equipment indexed by UID */
	private final Map<String, EquipmentInfo> equipmentById;

	/** Equipment grouped by type (e.g., "CoffeeMachine" -> list of instances) */
	private final Map<String, List<EquipmentInfo>> equipmentByType;

	/** Lock for thread-safe collection access */
	private final ReentrantLock lock;

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	/**
	 * Create a new EquipmentRegistry.
	 */
	public EquipmentRegistry() {
		this.equipmentById = new HashMap<>();
		this.equipmentByType = new HashMap<>();
		this.lock = new ReentrantLock();
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * Register a new equipment in the registry.
	 *
	 * @param info equipment information
	 */
	public void register(EquipmentInfo info) {
		lock.lock();
		try {
			equipmentById.put(info.uid, info);

			// Extract type from UID (e.g., "CoffeeMachine-1" -> "CoffeeMachine")
			String type = extractType(info.uid);
			equipmentByType.computeIfAbsent(type, k -> new ArrayList<>()).add(info);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Unregister equipment from the registry.
	 *
	 * @param uid unique identifier
	 */
	public void unregister(String uid) {
		lock.lock();
		try {
			EquipmentInfo info = equipmentById.remove(uid);
			if (info != null) {
				String type = extractType(uid);
				List<EquipmentInfo> typeList = equipmentByType.get(type);
				if (typeList != null) {
					typeList.remove(info);
					if (typeList.isEmpty()) {
						equipmentByType.remove(type);
					}
				}
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Get equipment information by UID.
	 *
	 * @param uid unique identifier
	 * @return equipment info or null if not found
	 */
	public EquipmentInfo get(String uid) {
		lock.lock();
		try {
			return equipmentById.get(uid);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Get all suspendable equipment sorted by priority (low priority first).
	 *
	 * <p>
	 * Returns equipment that:
	 * <ul>
	 * <li>Is not currently suspended</li>
	 * <li>Passes constraint check (allowsSuspension())</li>
	 * </ul>
	 * Sorted by priority: higher priority values (lower importance) first.
	 * </p>
	 *
	 * @return list of suspendable equipment
	 */
	public List<EquipmentInfo> getSuspendableEquipment() {
		lock.lock();
		try {
			return equipmentById.values().stream()
				.filter(EquipmentInfo::canSuspend)
				.sorted(Comparator.comparingInt((EquipmentInfo e) -> e.priority).reversed())
				.collect(Collectors.toList());
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Get all currently suspended equipment.
	 *
	 * @return list of suspended equipment
	 */
	public List<EquipmentInfo> getSuspendedEquipment() {
		lock.lock();
		try {
			return equipmentById.values().stream()
				.filter(e -> e.suspended)
				.collect(Collectors.toList());
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Get all equipment of a specific type.
	 *
	 * @param type equipment type (e.g., "CoffeeMachine")
	 * @return list of equipment of this type
	 */
	public List<EquipmentInfo> getByType(String type) {
		lock.lock();
		try {
			return new ArrayList<>(equipmentByType.getOrDefault(type, new ArrayList<>()));
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Get count of registered equipment.
	 *
	 * @return number of registered equipment
	 */
	public int size() {
		lock.lock();
		try {
			return equipmentById.size();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Get all registered equipment.
	 *
	 * @return list of all equipment info
	 */
	public List<EquipmentInfo> getAllEquipment() {
		lock.lock();
		try {
			return new ArrayList<>(equipmentById.values());
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Get all equipment UIDs.
	 *
	 * @return list of all UIDs
	 */
	public List<String> getAllUIDs() {
		lock.lock();
		try {
			return new ArrayList<>(equipmentById.keySet());
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Extract equipment type from UID.
	 *
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>"CoffeeMachine-1" -> "CoffeeMachine"</li>
	 * <li>"Fan-3" -> "Fan"</li>
	 * </ul>
	 * </p>
	 *
	 * @param uid unique identifier
	 * @return equipment type
	 */
	private String extractType(String uid) {
		// Handle "XxxGeneratedConnector" format from XML adapter registration
		if (uid.endsWith("GeneratedConnector")) {
			return uid.substring(0, uid.length() - "GeneratedConnector".length());
		}
		int dashIndex = uid.lastIndexOf('-');
		return dashIndex > 0 ? uid.substring(0, dashIndex) : uid;
	}

	/**
	 * Extract instance number from UID.
	 *
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>"CoffeeMachine-1" -> 1</li>
	 * <li>"Fan-3" -> 3</li>
	 * </ul>
	 * </p>
	 *
	 * @param uid unique identifier
	 * @return instance number or 0 if not found
	 */
	public static int extractInstance(String uid) {
		int dashIndex = uid.lastIndexOf('-');
		if (dashIndex > 0 && dashIndex < uid.length() - 1) {
			try {
				return Integer.parseInt(uid.substring(dashIndex + 1));
			} catch (NumberFormatException e) {
				return 0;
			}
		}
		return 0;
	}

	@Override
	public String toString() {
		lock.lock();
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("EquipmentRegistry[total=").append(equipmentById.size()).append("]:\n");
			for (String type : equipmentByType.keySet()) {
				sb.append("  ").append(type).append(": ").append(equipmentByType.get(type).size()).append("\n");
			}
			return sb.toString();
		} finally {
			lock.unlock();
		}
	}
}
