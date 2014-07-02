package dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.LinkedList;
import java.util.List;

public class PreparedStatementBuilder {

	protected String m_table=null;
	protected List<String> m_selectCols=null;
	protected List<String> m_insertCols=null;
	protected List<Object> m_insertVals=null;
	protected List<String> m_updateCols=null;
	protected List<Object> m_updateVals=null;
	protected List<String> m_whereCols=null;
	protected List<String> m_whereOps=null;
	protected List<Object> m_whereVals=null;
	protected String m_whereLink=null;
	protected List<String> m_orderBy=null;
	protected List<String> m_orientations=null;
	protected List<Integer> m_limits=null;

	public PreparedStatementBuilder(){
		
	}		

	public PreparedStatementBuilder from(String table){
		m_table=table;
		return this;
	}

	public PreparedStatementBuilder select(String col){
		if(m_selectCols==null) m_selectCols=new LinkedList<String>();
		m_selectCols.add(col);
		return this;
	}

	public PreparedStatementBuilder select(List<String> cols){
		for(String col : cols){
			this.select(col);
		}
		return this;
	}

	public PreparedStatementBuilder insert(String col, Object val){
		if(m_insertCols==null) m_insertCols=new LinkedList<String>();
		if(m_insertVals==null) m_insertVals=new LinkedList<Object>();
		m_insertCols.add(col);
		m_insertVals.add(val);
		return this;
	} 

	public PreparedStatementBuilder insert(List<String> cols, List<Object> vals){
		int n=cols.size();
		for(int i=0;i<n;i++){
			String col=cols.get(i);
			Object val=vals.get(i);
			this.insert(col, val);
		}
		return this;
	}

	public PreparedStatementBuilder update(String col, Object val){
		if(m_updateCols==null) m_updateCols=new LinkedList<String>();
		if(m_updateVals==null) m_updateVals=new LinkedList<Object>();
		m_updateCols.add(col);
		m_updateVals.add(val);
		return this;
	} 

	public PreparedStatementBuilder update(List<String> cols, List<Object> vals){
		int n=cols.size();
		for(int i=0;i<n;i++){
			String col=cols.get(i);
			Object val=vals.get(i);
			this.update(col, val);
		}
		return this;
	}
	public PreparedStatementBuilder where(String col, String op, Object val){
		if(m_whereCols==null) m_whereCols=new LinkedList<String>();
		if(m_whereOps==null) m_whereOps=new LinkedList<String>();
		if(m_whereVals==null) m_whereVals=new LinkedList<Object>();
		m_whereCols.add(col);
		m_whereOps.add(op);
		m_whereVals.add(val);
		return this;
	}

	public PreparedStatementBuilder where(List<String> cols, List<String> ops, List<Object> vals){
		if(cols==null || ops==null || vals==null) return this;
		if(cols.size()!=ops.size() || cols.size()!=vals.size()) return this;
		int n=cols.size();
		for(int i=0;i<n;i++){
			String col=cols.get(i);
			String op=ops.get(i);
			Object val=vals.get(i);
			this.where(col, op, val);
		}
		return this;
	}

	public PreparedStatementBuilder where(String whereLink){
		m_whereLink=whereLink;
		return this;
	}

	public PreparedStatementBuilder order(String col, String orientation){
		if(m_orderBy==null) m_orderBy=new LinkedList<String>();
		m_orderBy.add(col);

		if(orientation==null) return this;
		if(m_orientations==null) m_orientations=new LinkedList<String>();
		m_orientations.add(orientation);
		return this;
	}
		
	public PreparedStatementBuilder order(String col){
		this.order(col, null);
		return this;
	}

	public PreparedStatementBuilder order(List<String> cols, List<String> orientations){
		int n=cols.size();
		for(int i=0;i<n;i++){
			String col=cols.get(i);
			String orientation=null;
			if(orientations!=null){
				orientation=orientations.get(i);
			}
			this.order(col, orientation);
		}
		return this;
	}
	
	public PreparedStatementBuilder order(List<String> cols){
		this.order(cols, null);
		return this;
	}

	public PreparedStatementBuilder limit(int st, int ed){
		if(m_limits==null) m_limits=new LinkedList<Integer>();
		m_limits.clear();
		m_limits.add(st);	
		m_limits.add(ed);
		return this;
	}
	
	public PreparedStatementBuilder limit(int num){
		if(m_limits==null) m_limits=new LinkedList<Integer>();
		m_limits.clear();
		m_limits.add(num);
		return this;
	}
	
	protected String appendTable(String query){
		String ret=query+" FROM "+m_table;
		return ret;	
	}

	protected String appendWhere(String query){
		String ret=query;
		if(m_whereCols!=null){
			ret+=" WHERE ";
			for(int i=0;i<m_whereCols.size();i++){
				String col=m_whereCols.get(i);
				String op=m_whereOps.get(i);
				ret+=(col+op+"?");
				if(i<m_whereCols.size()-1){
					if(m_whereLink==null) ret+=" AND ";
					else ret+=(" "+m_whereLink+" ");
				}
			}
		}
		return ret;
	}
	
	protected String appendOrder(String query){
		String ret=query;
		if(m_orderBy!=null){
			ret+=" ORDER BY ";
			for(int i=0;i<m_orderBy.size();i++){
				String col=m_orderBy.get(i);
				ret+=col;
				if(m_orientations.size()>i){
					String orientation=m_orientations.get(i);
					ret+=" "+orientation;
				}
				if(i<m_orderBy.size()-1) ret+=", ";
			}
		}
		return ret;
	}

	protected String appendLimit(String query){
		String ret=query;
		if(m_limits!=null){
			ret+=" LIMIT ";
			for(int i=0;i<m_limits.size();i++){
				ret+="?";
				if(i<m_limits.size()-1) ret+=", ";
			}
		}
		return ret;
	}

	public PreparedStatement toSelect(Connection connection){
	
		if(connection==null) return null;
		PreparedStatement statement=null;
		try{	
			String query="SELECT ";
			
			for(int i=0;i<m_selectCols.size();i++){
				String col=m_selectCols.get(i);
				query+=col;
				if(i<m_selectCols.size()-1) query+=", ";
			}

			query=appendTable(query);
			query=appendWhere(query);
			query=appendOrder(query);
			query=appendLimit(query);

			int index=1;

			statement= connection.prepareStatement(query);	
			if(m_whereVals!=null){
				for(Object val : m_whereVals){
					statement.setObject(index++, val);
				}
			}
			if(m_limits!=null){
				for(Integer limit : m_limits){
					statement.setInt(index++, limit);
				}
			}
			
		} catch (Exception e){
			System.out.println("PreparedStatementBuilder, "+e.getMessage());
		}
		return statement;
	}
	
}
