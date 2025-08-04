package com.ezdata.cdsportal.web.user.svauser;

import com.ezdata.cdsportal.metadata.Constant;
import com.ezdata.cdsportal.web.component.selectuser.DelegateUser;
import com.ezdata.cdsportal.web.component.selectuser.UserList;
import com.ezdata.db.Query;
import com.ezdata.metadata.ISSDefine;
import com.ezdata.web.PageEngine;
import com.ezdata.web.SearchEvent;
import com.ezdata.web.URL;

/**
 * UpdatedUserList.java
 * 
 * @author Anjali Sharma
 * 
 * @created October 16, 2024 01:15:00 PM
 */

public class SVAUserRolesList extends UserList {
	
	/** The unique identifier for this component. */
	public static final String COMPONENT_ID = "SVAUserRolesList";
	
	/**
	 * Constructs an instance of UpdatedUserList.
	 * @param eng {@link PageEngine} object
	 * @throws Exception when error occurs.
	 */
	public SVAUserRolesList(PageEngine eng) throws Exception {
		super(eng);
	}
	
	@Override
	public void init() throws Exception {
		super.init();
		if(this.m_delegate instanceof DelegateUser) {
			((DelegateUser)this.m_delegate).m_using_userproxy = false;
		}		
	}
	
	@Override
	public ISSDefine getSSDefine() throws Exception {
		return Constant.SVAUSERROLESJOB_SUM_SS;
    }
	
	@Override
	public void beforeSearch(SearchEvent evnt) throws Exception {
		Query query = evnt.getQuery();
		
		query.eq(Constant.SVAUSERROLESJOB_OfficeId, m_eng.getRequestLong(URL.OFFICEID));
		query.eq(Constant.SVAUSERROLESJOB_TransactionId, m_eng.getRequest(URL.createColIDURL(Constant.SVAUSERROLESJOB_TransactionId)));
		query.eq(Constant.SVAUSERROLESJOB_Status, m_eng.getRequestInt(URL.createColIDURL(Constant.SVAUSERROLESJOB_Status)));
		super.beforeSearch(evnt);
	}
}