package shyview;

public interface IPicList extends java.util.List<IPicture> {
	String getName();
	IPicture next();
	IPicture current();
	IPicture previous();
	int getIndex();
	void setIndex(int i);
	void cleanup();
	void sort();
	ShyviewMenu getMenuItem();
}
