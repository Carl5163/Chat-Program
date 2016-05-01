import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

@SuppressWarnings("serial")
public class MyTableModel extends AbstractTableModel implements ListDataListener {

	DefaultListModel<BuddyInfo> listModel;
	public static final String[] ColumnNames = {"Name", "Status"};
	private static final String[] STATUS_STRINGS = {"Online", "Offline"};
	
	public MyTableModel() {
		listModel = new DefaultListModel<BuddyInfo>();
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
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		BuddyInfo bi = listModel.get(rowIndex);
		Object ret = null;
		if(columnIndex == 0) {
			ret = bi.name;
		}
		if(columnIndex == 1) {
			ret = STATUS_STRINGS[bi.status];
		}
		return ret;
	}
	
	public DefaultListModel<BuddyInfo> getDefaultListModel() {
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

	public void add(BuddyInfo bi) {
		listModel.addElement(bi);				
	}

	public void clear() {
		listModel.clear();
	}

}
