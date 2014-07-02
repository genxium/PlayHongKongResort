package dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.LinkedList;
import java.util.List;

class PreparedStatementBuilder {
	protected PreparedStatement m_statement=null;
	protected String m_table=null;
	protected List<String> m_selectCols=null;
	protected List<String> m_insertCols=null;
	protected List<Object> m_insertVals=null;
	protected List<String> m_updateCols=null;
	protected List<Object> m_updateVals=null;
	protected List<String> m_whereCols=null;
	protected List<String> m_whereOps=null;
	protected List<Object> m_whereVals=null;
	protected List<String> m_orderBy=null;
	protected List<String> m_orientations=null;
	protected List<Integer> m_limits=null;

	public PreparedStatementBuilder(Connection connection){

	}		

	public void from(String table){
		m_table=table;
	}

	public void select(String col){
		if(m_selectCols==null) m_selectCols=new LinkedList<String>();
		m_selectCols.add(col);
	}

	public void select(List<String> cols){
		for(String col : cols){
			select(col);
		}
	}

	public void insert(String col, Object val){
		if(m_insertCols==null) m_insertCols=new LinkedList<String>();
		if(m_insertVals==null) m_insertVals=new LinkedList<Object>();
		m_insertCols.add(col);
		m_insertVals.add(val);
	} 

	public void insert(List<String> cols, List<Object> vals){
		int n=cols.size();
		for(int i=0;i<n;i++){
			String col=cols.get(i);
			Object val=vals.get(i);
			insert(col, val);
		}
	}

	public void update(String col, Object val){
		if(m_updateCols==null) m_updateCols=new LinkedList<String>();
		if(m_updateVals==null) m_updateVals=new LinkedList<Object>();
		m_updateCols.add(col);
		m_updateVals.add(val);
	} 

	public void update(List<String> cols, List<Object> vals){
		int n=cols.size();
		for(int i=0;i<n;i++){
			String col=cols.get(i);
			Object val=vals.get(i);
			update(col, val);
		}
	}
	public void where(String col, String op, Object val){
		if(m_whereCols==null) m_whereCols=new LinkedList<String>();
		if(m_whereOps==null) m_whereOps=new LinkedList<String>();
		if(m_whereVals==null) m_whereVals=new LinkedList<Object>();
		m_whereCols.add(col);
		m_whereOps.add(op);
		m_whereVals.add(val);
	}

	public void where(List<String> cols, List<String> ops, List<Object> vals){
		if(cols==null || ops==null || vals==null) return;
		if(cols.size()!=ops.size() || cols.size()!=vals.size()) return;
		int n=cols.size();
		for(int i=0;i<n;i++){
			String col=cols.get(i);
			String op=ops.get(i);
			Object val=vals.get(i);
			where(col, op, val);
		}
	}

	public void order(String col, String orientation){
		if(m_orderBy==null) m_orderBy=new LinkedList<String>();
		m_orderBy.add(col);

		if(orientation==null) return;
		if(m_orientations==null) m_orientations=new LinkedList<String>();
		m_orientations.add(orientation);
	}
		
	public void order(String col){
		order(col, null);
	}

	public void order(List<String> cols, List<String> orientations){
		int n=cols.size();
		for(int i=0;i<n;i++){
			String col=cols.get(i);
			String orientation=null;
			if(orientations!=null){
				orientation=orientations.get(i);
			}
			order(col, orientation);
		}
	}
	
	public void order(List<String> cols){
		order(cols, null);
	}

	public void limit(int st, int ed){
		if(m_limits==null) m_limits=new LinkedList<Integer>();
		m_limits.clear();
		m_limits.add(st);	
		m_limits.add(ed);
	}
	
	public void limit(int num){
		if(m_limits==null) m_limits=new LinkedList<Integer>();
		m_limits.clear();
		m_limits.add(num);
	}
}
