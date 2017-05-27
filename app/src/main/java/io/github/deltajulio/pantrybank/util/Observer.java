package io.github.deltajulio.pantrybank.util;

public interface Observer
{
	public void OnNotify(Observable observable, Object event);
}
