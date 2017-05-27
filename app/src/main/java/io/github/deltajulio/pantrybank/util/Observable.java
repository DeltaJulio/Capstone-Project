package io.github.deltajulio.pantrybank.util;

import java.util.HashSet;

/**
 * Implementation should use HashSet to keep track of observers
 */
public interface Observable
{
	public void AddObserver(Observer o);

	public void RemoveObserver(Observer o);

	public void Notify();
}
