


import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;


@SuppressWarnings("serial")
public class MyTableModelRequests extends AbstractTableModel implements ListDataListener {

	DefaultListModel<String> listModel;
	public static final String[] ColumnNames = {"Name"};
	
	public MyTableModelRequests() {
		listModel = new DefaultListModel<String>();
		listModel.addListDataListener(this);
	}
	
	@Override
	public String getColumnName(int i) {
		return ColumnNames[i];
	}
	
	@Override
	public int getRowCount() {
		return listModel.size();
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object ret = null;
		if(columnIndex == 0) {
			ret = listModel.get(rowIndex);
		}
		return ret;
	}
	
	public DefaultListModel<String> getDefaultListModel() {
		return listModel;
	}

	@Override
	public void contentsChanged(ListDataEvent arg0) {
		fireTableDataChanged();		
	}

	@Override
	public void intervalAdded(ListDataEvent arg0) {
		fireTableDataChanged();		
	}
	@Override
	public void intervalRemoved(ListDataEvent arg0) {
		fireTableDataChanged();		
	}

	public void add(String bi) {
		boolean bad = false;
		for(int i = 0; i < listModel.size(); i++) {
			if(listModel.getElementAt(i).equalsIgnoreCase(bi)) {
				bad = true;
			}
		}
		if(!bad) {
			listModel.addElement(bi);
		}				
	}
	
	public void remove(String bi) {
		for(int i = 0; i < listModel.size(); i++) {
			if(listModel.getElementAt(i).equalsIgnoreCase(bi)) {
				listModel.remove(i);
			}
		}
	}

	public void clear() {
		listModel.clear();
	}

}
