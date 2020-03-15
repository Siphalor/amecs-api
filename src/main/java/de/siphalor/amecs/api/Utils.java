package de.siphalor.amecs.api;

@SuppressWarnings("WeakerAccess")
public class Utils {
	/**
	 * Sets a flag to a value
	 * @param base the base value
	 * @param flag the flag to set/unset
	 * @param val whether the flag gets set/unset
	 * @return the new value
	 */
	@Deprecated
	public static char setFlag(char base, char flag, boolean val) {
		return val ? setFlag(base, flag) : removeFlag(base, flag);
	}

	/**
	 * Sets a flag on a value
	 * @param base the base value
	 * @param flag the flag to set
	 * @return the new value
	 */
	@Deprecated
	public static char setFlag(char base, char flag) {
		return (char) (base | flag);
	}

	/**
	 * Unsets a flag on a value
	 * @param base the base value
	 * @param flag the flag to unset|remove
	 * @return the new value
	 */
	@Deprecated
	public static char removeFlag(char base, char flag) {
		return (char) (base & (~flag));
	}

	/**
	 * Gets the state of a flag
	 * @param base the base value
	 * @param flag the flag to evaluate
	 * @return whether the flag is set on the base value
	 */
	@Deprecated
	public static boolean getFlag(char base, char flag) {
		return (base & flag) != 0;
	}
}
