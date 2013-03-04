package shyview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class PictureList extends LinkedList<IPicture> implements IPicList{
	private static final long serialVersionUID = 1L;
	
	String name = "Default";
	int index = 0;
	ShyviewMenu menuItem = null;
	

	public PictureList(String parname) {
		name = parname;
	}

	public String getName() {
		return name;
	}
	
	public IPicture next() {
		if (current() != null) current().interrupt();
		index++;
		if (isIndexInRange(index+1)) get(index + 1).preload();
		return current();
	}
	
	public IPicture current() {
		if (index >= this.size() || index < 0) return null;
		return this.get(index);
	}

	@Override
	public IPicture previous() {
		if (current() != null) current().interrupt();
		index--;
		if (isIndexInRange(index-1)) get(index - 1).preload();
		return current();
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public void setIndex(int i) {
		index = i;
	}

	/**
	 * Removes double entrys.
	 */
	@Override
	public void cleanup() {
		ArrayList<IPicture> newlist = new ArrayList<IPicture>();
		for (IPicture p1: this) {
			boolean test = true;
			for (IPicture p2: newlist) {
				if (p1.equals(p2)) test = false;
			}
			if (test) newlist.add(p1);
		}
		this.clear();
		this.addAll(newlist);
	}

	@Override
	public void sort() {
		ShyviewComparator.PictureComparator comp = (new ShyviewComparator()).new PictureComparator();
		Collections.sort(this, comp);
	}

	@Override
	public ShyviewMenu getMenuItem() {
		if (menuItem == null)
			menuItem = new ShyviewMenu(this);
		
		return menuItem;
	}
	
	private boolean isIndexInRange(int index) {
		return (index >= 0 && index < size());
	}
	
}
