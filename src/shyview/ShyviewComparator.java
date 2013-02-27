package shyview;

import java.util.Comparator;

public class ShyviewComparator {
	class PictureComparator implements Comparator<IPicture> {
		@Override
		public int compare(IPicture arg0, IPicture arg1) {
			return compareString(arg0.getName(), arg1.getName());
		}
	}
	class ListComparator implements Comparator<IPicList> {
		@Override
		public int compare(IPicList o1, IPicList o2) {
			return compareString(o1.getName(), o2.getName());
		}
	}
	
	public int compareString(String unit1, String unit2) {
		int firstdif = this.firstDifference(unit1, unit2);
		unit1 = unit1.substring(firstdif, unit1.length());
		unit2 = unit2.substring(firstdif, unit2.length());
		int min = getMinNumberLength(unit1, unit2);
		int compare = 0;
		if (min > 0) {
			Double intpic1 = getNumber(unit1);
			Double intpic2 = getNumber(unit2);
			compare = intpic1.compareTo(intpic2);
		} else {
			compare = unit1.compareToIgnoreCase(unit2);
		}
		return compare;
	}
	
	public int getNumberLength(String str, int startpos) {
		int pos = 0;
		while (pos + startpos != str.length()) {
			char test = str.charAt(pos + startpos);
			if (!Character.isDigit(test)) break;  
			pos++;
		}
		return pos;
	}
	
	private int getMinNumberLength(String a, String b) {
		int cha = getNumberLength(a,0);
		int chb = getNumberLength(b,0);
		return (cha < chb) ? cha : chb;
	}
	
	private double getNumber(String str) {
		int length = getNumberLength(str,0);
		String numb = (String) str.subSequence(0, length);
		double re = 0;
		try {
			re = Double.valueOf(numb).doubleValue();
			//re = Integer.valueOf(numb).intValue();
		} catch (java.lang.NumberFormatException e) {
			e.printStackTrace();
		}
		return re;
	}
	private int firstDifference(String a, String b) {
		int minlength = (a.length() < b.length()) ? a.length() : b.length();
		int i = 0;
		for (i = 0; i < minlength; i++) {
			if (a.charAt(i) != b.charAt(i)) break;
		}
		return i;
	}
}
