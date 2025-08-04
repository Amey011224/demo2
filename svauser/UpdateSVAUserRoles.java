package com.ezdata.cdsportal.web.user.svauser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import com.ezdata.base.framework.session.SessionAttribute;
import com.ezdata.base.resource.IResourceWorker;
import com.ezdata.base.resource.ResourceManager;
import com.ezdata.cdsportal.metadata.Constant;
import com.ezdata.cdsportal.metadata.Lookup;
import com.ezdata.cdsportal.system.Session;
import com.ezdata.cdsportal.web.component.SimpleSS;
import com.ezdata.cdsportal.web.component.SimpleSSRow;
import com.ezdata.cdsportal.web.homecontent.XMLLayoutHelper;
import com.ezdata.cdsportal.web.user.svauser.SVAEnabledUserRoles.SVAEnabledUserRole;
import com.ezdata.db.DataShuttle;
import com.ezdata.db.Query;
import com.ezdata.system.ISession;
import com.ezdata.system.security.IRole;
import com.ezdata.system.security.IRoleGroup;
import com.ezdata.system.security.Role;
import com.ezdata.util.Array;
import com.ezdata.util.DateUtil;
import com.ezdata.util.LocaleManager;
import com.ezdata.util.Log;
import com.ezdata.util.Str;
import com.ezdata.util.UniqueIntArray;
import com.ezdata.web.AnchorEvent;
import com.ezdata.web.AnchorEventListener;
import com.ezdata.web.IBaseComponent;
import com.ezdata.web.PageEngine;
import com.ezdata.web.component.AnchorComponent;
import com.ezdata.web.component.Component;
import com.ezdata.web.component.ToolbarComponent;
import com.ezdata.xml.NameIdFilter;
import com.ezdata.xml.XMLHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * UpdateSVAUserRoles.java
 * 
 * @author Akash Shinde
 * 
 * September 2, 2024 01:15:00 PM
 * Copyright Zinnia Distributor Solutions LLC 2024. All rights reserved.
 * ZINNIA PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

public class UpdateSVAUserRoles extends Component implements IBaseComponent, AnchorEventListener {

	/** Constant variable {@link Logger} used for logging events and errors. */
	private static final Logger m_logger = Log.getLogger(Log.PORTAL, UpdateSVAUserRoles.class);

	/** Variable {@code String} to store path to the JSON file containing SVA-enabled user roles data. */
	private static final String SVA_ENABLED_ROLES_FILE_PATH = "/com/ezdata/cdsportal/data/svaroles/svaenabledroles.json";
	
	/**	Constant {@code String} variable representing the identifier for the first role template. */
	private static final String ROLE_TEMPLATE1 = "roleTemplate1";
	
	/**	Constant {@code String} variable representing the identifier for the second role template. */
	private static final String ROLE_TEMPLATE2 = "roleTemplate2";
	
	/**	Constant {@code String} variable for roleActionType. */
	private static final String ROLE_ACTION_TYPE = "roleActionType";
	
	/**	Constant {@code String} variable for role. */
	private static final String ROLE = "role";
	
	/**	Constant {@code String} variable for ezscript. */
	private static final String EZSCRIPT = "ezscript";
	
	/**	Constant {@code String} variable for taggedIds. */
	private static final String TAGGEDIDS = "taggedIds";
	
	/**	Constant {@code String} variable for selectedRoles. */
	private static final String SELECTED_ROLES = "selectedRoles";
	
	/**	Constant {@code String} representing the action type for adding roles. */
	private static final String FROM_ROLE_ACTION_ADD = "fromRoleActionAdd";
	
	/**	Constant {@code String} variable for COLUMN_ADMIN role. */
	private static final int ROLE_COLUMN_ADMIN = 137;

	/** Variable {@link ISession} to store session object. */
	private ISession m_session;

	/** Variable {@code String} to identify the action type for role operations (add/remove). */
	private String m_roleActionType;

	/** A static final array {@link UniqueIntArray} that holds the Id's of SVA enabled roles */
	private static final UniqueIntArray SVA_ENABLED_ROLES_ARR = new UniqueIntArray();
	
	/** A {@link ISession} temporary session instance used for managing session-related operations. */
	private ISession m_tempLoginSession;

	/**
	 * Static initializer block for the UpdateSVAUserRoles class.
	 * This static block is executed when the class is loaded, and it calls
	 * {@link #loadRoles()} to load SVA enabled roles from the JSON file.
	 * This ensures that the roles are available for use throughout the class as soon as the class is loaded.
	 */
	static {
		loadRoles();
	}

	/**
	 * Constructs an instance of UpdateSVAUserRoles.
	 * @param eng {@link PageEngine} object
	 */
	public UpdateSVAUserRoles(PageEngine eng) {
		super(eng);
	}

	/**
	 * This method reads the SVA enabled roles from a specified JSON file 
	 * and deserializes the content into an instance of {@link SVAEnabledUserRoles}. 
	 * It then extracts the list of enabled user roles and calls 
	 * {@link #loadSVAEnabledRoles(List)} to process them.
	 */
	private static void loadRoles() {
		try(InputStreamReader reader = new InputStreamReader(UpdateSVAUserRoles.class.getResourceAsStream(SVA_ENABLED_ROLES_FILE_PATH))) {
			// Deserialize the JSON directly into SVAEnabledUserRoles
			SVAEnabledUserRoles svaEnabledUserRoles = new ObjectMapper().readValue(reader, SVAEnabledUserRoles.class);

			List<SVAEnabledUserRole> svaEnabledRolesList = svaEnabledUserRoles.getSVAEnabledUserRoles();
			loadSVAEnabledRoles(svaEnabledRolesList);

		}catch(IOException ioe) {
			m_logger.error("Unable to load the svaenabledroles.json file.", ioe);
		}catch(Exception ex) {
			m_logger.error("An error occured while loading SVA Enabled roles.", ex);
		}
	}

	/**
	 * Loads SVA enabled roles into the UniqueIntArray.
	 * @param svaEnabledRolesList A list of {@link SVAEnabledUserRole} instances to populate with enabled role Id's.
	 * @throws Exception If an error occurs during the loading process.
	 */
	private static void loadSVAEnabledRoles(List<SVAEnabledUserRole> svaEnabledRolesList) throws Exception {
		if(svaEnabledRolesList == null || svaEnabledRolesList.isEmpty()) {
			m_logger.info("UpdateSVAUserRoles - loadSVAEnabledRoles() :: svaEnabledRolesList is null/empty.");
			return;
		}
		svaEnabledRolesList.forEach(role -> SVA_ENABLED_ROLES_ARR.add(role.getRoleId()));

		String integrationRoleName = LocaleManager.getLocale(LocaleManager.getDefaultLocale().getName()).getString("~Integration");
		if(Str.isBlank(integrationRoleName)) {
			m_logger.info("UpdateSVAUserRoles - loadSVAEnabledRoles() :: Integration role name is null, so integration roles have not been loaded.");
			return;
		}

		Role.iterator().forEachRemaining(role -> {
			IRoleGroup roleGroup = role.getGroup();
			String roleName = roleGroup != null ? roleGroup.getName() : null;
			if(!Str.isBlank(roleName) && roleName.equalsIgnoreCase(integrationRoleName)) {
				SVA_ENABLED_ROLES_ARR.add(role.getId());
			}
		});
	}

	@Override
	public void init() throws Exception {
		m_session = m_eng.getSession();
		m_roleActionType = m_eng.getRequest(ROLE_ACTION_TYPE);
		
		if(Str.isBlank(m_roleActionType)) {
		    m_eng.insertScript("user/svauser/updatesvaroles");
		}
	}

	@Override
	public Element run(Element param) throws Exception {
		if (!Str.isBlank(m_roleActionType)) {
			try {
				m_tempLoginSession = Session.doLogin(SessionAttribute.withId(m_session.getLoginOfficeId(), m_session.getUserId()).setTempSession(true));
				createSVAUserRolesJob();
			} catch (Exception ex) {
				m_logger.error("UpdateSVAUserRoles: run() :: Error occured while adding SVA User Roles : {}", ex.getMessage(), ex);
			} finally {
				if (m_tempLoginSession != null) {
					m_tempLoginSession.drop();
				}
			}
		} else {
			loadRolesAndGenerateUI();
		}
		return m_root;
	}

	/**
	 * Creates a job for SVA user roles.
	 *
	 * This method initializes a data shuttle to prepare a new SVA user roles job
	 * using the provided transaction ID and tagged user IDs. It logs the creation
	 * details and inserts the relevant records into the database.
	 *
	 * @throws Exception If an error occurs during the execution of the job.
	 */
	private void createSVAUserRolesJob() throws Exception {
		ResourceManager.run(new IResourceWorker() {
			@Override
			public void exec() throws Exception {
				String transactionId = UUID.randomUUID().toString().toUpperCase();

				DataShuttle shuttle = new DataShuttle(Constant.SVAUSERROLESJOB_TABLE, null);
				Query query = new Query(Constant.SVAUSERROLESJOB_TABLE, shuttle);
				query.forPreparedQuery(ResourceManager.getManager());
				m_tempLoginSession.initQuery(query);

				shuttle.appendRow();
				populateDataShuttle(shuttle, transactionId);
				processTaggedIds(shuttle, query);
			}
		});
	}
	
	/**
	 * Populates the given DataShuttle with userId's, user roles and transaction information.
	 *
	 * This method sets the created by office ID, created by user ID, transaction ID,
	 * and selected roles in the provided DataShuttle instance. It retrieves the
	 * current user's office ID and user ID from the session, and the selected roles
	 * from the request.
	 *
	 * @param shuttle The DataShuttle instance to populate with values.
	 * @param transactionId The transaction ID to set in the shuttle.
	 * @return The populated DataShuttle instance with user and transaction data.
	 * @throws Exception If an error occurs during the population process.
	 */
	private DataShuttle populateDataShuttle(DataShuttle shuttle, String transactionId) throws Exception {
		int roleAction = (FROM_ROLE_ACTION_ADD.equals(m_roleActionType)) ? Lookup.LUV_SVAROLESJOBACTION_Add : Lookup.LUV_SVAROLESJOBACTION_Remove;
		
	    shuttle.setLongValue(0, Constant.SVAUSERROLESJOB_CreatedByOfficeId, m_tempLoginSession.getLoginOfficeId());
	    shuttle.setLongValue(0, Constant.SVAUSERROLESJOB_CreatedById, m_tempLoginSession.getUserId());
	    shuttle.setStringValue(0, Constant.SVAUSERROLESJOB_TransactionId, transactionId);
	    shuttle.setStringValue(0, Constant.SVAUSERROLESJOB_Roles, m_eng.getRequest(SELECTED_ROLES));
	    shuttle.setIntValue(0, Constant.SVAUSERROLESJOB_Action, roleAction);
	    shuttle.setDateValue(0, Constant.SVAUSERROLESJOB_CreatedOn, DateUtil.now());
	    
	    return shuttle;
	}
	
	/**
	 * Processes tagged IDs to set user office and user ID in the DataShuttle.
	 *
	 * This method retrieves tagged IDs from the request, splits them into office and user IDs,
	 * and updates the provided DataShuttle instance accordingly. It logs a message if either
	 * the office ID or user ID is not present.
	 *
	 * @param shuttle The DataShuttle instance to update with office and user IDs.
	 * @param query The Query instance used for inserting records into the database.
	 * @throws Exception If an error occurs during the processing of tagged IDs.
	 */
	private void processTaggedIds(DataShuttle shuttle, Query query) throws Exception {
		String[] taggedIdsArr = m_eng.getRequestValues(TAGGEDIDS);
	    for (String taggedId : taggedIdsArr) {
	        String[] taggedIdArr = taggedId.split("_");
	        if (taggedIdArr.length > 1) {
	            long officeId = Long.parseLong(taggedIdArr[0]);
	            long userId = Long.parseLong(taggedIdArr[1]);
	            if (officeId == 0 || userId == 0) {
	                m_logger.info("UpdateSVAUserRoles: updateUserRoles() :: OfficeId or UserId is not present. OfficeId: {}, userId: {}", officeId, userId);
	                continue;
	            }
	            shuttle.setLongValue(0, Constant.SVAUSERROLESJOB_OfficeId, officeId);
	            shuttle.setLongValue(0, Constant.SVAUSERROLESJOB_AppUserId, userId);
	            query.insert();
	        }
	    }
	}

	/**
	 * Loads the role template from an XML file and generates the user interface.
	 * @throws Exception If an error occurs while loading the XML file or during UI generation.
	 */
	private void loadRolesAndGenerateUI() throws Exception {
		m_root = m_eng.loadForm("user/svauser/svarolestemplate.xml", null);
		generateUI();
	}

	/**
	 * Generates the user interface components for SVA role.
	 * @throws Exception If an error occurs during the generation of the UI components or while filling the role data.
	 */
	private void generateUI() throws Exception {
		SimpleSS[] svaRoleSSArr = new SimpleSS[] {new SimpleSS(m_eng, ROLE_TEMPLATE1), new SimpleSS(m_eng, ROLE_TEMPLATE2)};
		for(int ssIndex = 0; ssIndex < svaRoleSSArr.length; ssIndex++) {
			svaRoleSSArr[ssIndex].drawCheckbox();
			svaRoleSSArr[ssIndex].setSSHeight(400);
			svaRoleSSArr[ssIndex].appendCol(ssIndex == 0 ? m_eng.getString("~ssHeadUserRole") : m_eng.getString("~ssHeadMouelesLicenses"), 330, 0, null, null);
			svaRoleSSArr[ssIndex].setSSWidth(375);
		}
		fillSimpleSS(svaRoleSSArr);
	}	

	/**
	 * Fills an array of SimpleSS objects with role data and updates the corresponding XML elements.
	 * @param svaRoleSSArr An array of {@link SimpleSS} objects to be filled with role data.
	 * @throws Exception If an error occurs during processing or XML operations.
	 */
	private void fillSimpleSS(SimpleSS[] svaRoleSSArr) throws Exception {
	    if(svaRoleSSArr == null || svaRoleSSArr.length < 1) {
	        return;
	    }
	    
	    Set<IRoleGroup> groups = new HashSet<>();
	    StringBuilder buffer = new StringBuilder();

	    Element roleTemplateNode1 = (Element) XMLHelper.selectSingleNode(m_root, new NameIdFilter(XMLLayoutHelper.TAG_TD, ROLE_TEMPLATE1));
	    Element roleTemplateNode2 = (Element) XMLHelper.selectSingleNode(m_root, new NameIdFilter(XMLLayoutHelper.TAG_TD, ROLE_TEMPLATE2));

	    Iterator<IRole> iterator = Role.iterator();
	    while(iterator.hasNext()) {
	        IRole role = iterator.next();
	        if(!(SVA_ENABLED_ROLES_ARR.indexOf(role.getId()) != Array.NPOS && isValidRole(role))) {
	            continue;
	        }

	        processRoleDependencies(role, buffer);
	        processRoleParents(role, buffer);

	        if(isBaseGroupOrSpecialRole(role)) {
	            continue;
	        }

	        SimpleSSRow row = appendRoleToBuffer(role, buffer);
	        appendRowToSSArray(row, svaRoleSSArr, role);
	        groups.add(role.getGroup());
	    }
	    
	    commitRoleTemplate(svaRoleSSArr, roleTemplateNode1, roleTemplateNode2, buffer, groups);
	}

	/**
	 * Checks if the provided role is valid based on specific conditions
	 * @param role {@link IRole} object to be validated.
	 * @return {@code true} if the role is valid; {@code false} otherwise.
	 */
	private boolean isValidRole(IRole role) {
	    try{
            return role != null && !role.isHidden() && m_session.checkSecurity(null, (Set) role.getRuntimeConditions());
        }catch(Exception e) {
        	m_logger.error("UpdateSVAUserRoles - isValidRole() :: Error occured while checking valid role condition.", e);
            return false; 
        }
	}

	/**
	 * Processes and appends dependent roles for a given role to a StringBuilder buffer.
	 * @param role {@link IRole} object for which dependencies are processed.
	 * @param buffer {@link StringBuilder} used to construct the output script.
	 * @throws Exception If an error occurs while processing the role dependencies.
	 */ 
	private void processRoleDependencies(IRole role, StringBuilder buffer) throws Exception{
	    Set<Object> depends = role.getDepends();
	    if(depends == null) {
	    	return;
	    }

	    List<IRole> validRoles = depends.stream() //Stream Creation:
	        .filter(obj -> obj instanceof IRole) // filters the stream to retain only those objects that are instances of the IRole interface.
	        .map(obj -> (IRole) obj) // After filtering, this step casts the remaining objects to the IRole type. Since the filter guarantees that the objects are of type IRole.
	        .filter(depRole -> isValidRole(depRole)) // method is called to check if the role meets valid criteria.
	        .collect(Collectors.toList()); // Finally, the stream is collected back into a List<IRole> containing all the valid roles.

	    if(!validRoles.isEmpty()) {
	    	buffer.append("addDepends(").append(role.getId()).append(",[");
	        buffer.append(validRoles.stream().map(IRole::getId).map(String::valueOf).collect(Collectors.joining(",")));
	        buffer.append("]);");
	    }
	}

	/**
	 * Processes and appends parent roles for a given role to a StringBuilder buffer.
	 * @param role {@link IRole} object for which parent roles are processed.
	 * @param buffer {@link StringBuilder} used to construct the output script.
	 * @throws Exception If an error occurs while processing the role parents.
	 */
	private void processRoleParents(IRole role, StringBuilder buffer) throws Exception {
	    Set<Object> parents = role.getParents();
	    if(parents == null || parents.isEmpty()) {
	    	return;
	    }

	    List<IRole> validParents = parents.stream()
	        .filter(obj -> obj instanceof IRole)
	        .map(obj -> (IRole) obj)
	        .filter(parentRole -> isValidRole(parentRole))
	        .collect(Collectors.toList());

	    if(!validParents.isEmpty()) {
	        buffer.append("addParent(").append(role.getId()).append(",[");
	        buffer.append(validParents.stream().map(IRole::getId).map(String::valueOf).collect(Collectors.joining(",")));
	        buffer.append("]);");
	    }
	}

	/**
	 * Checks if the given role is part of the base group or is a special role.
	 * @param role {@link IRole} object to be checked.
	 * @return {@code true} if the role is part of the base group or is a special role; {@code false} otherwise.
	 */
	private boolean isBaseGroupOrSpecialRole(IRole role) {
	    return "baseGroup".equalsIgnoreCase(role.getGroup().getName()) || role.getId() == ROLE_COLUMN_ADMIN;
	}

	/**
	 * Appends role information to a StringBuilder buffer for script generation
	 * @param role {@link IRole} object representing the role to be append.
	 * @param buffer {@link StringBuilder} object used to construct the output script
	 * @return {@link SimpleSSRow} representing the appended role data.
	 * @throws Exception If an error occurs while processing the role or appending data.
	 */
	private SimpleSSRow appendRoleToBuffer(IRole role, StringBuilder buffer) throws Exception {
		SimpleSSRow row = new SimpleSSRow(m_eng, ROLE, Long.toString(role.getId()), SVA_ENABLED_ROLES_ARR.indexOf(role.getId()) != Array.NPOS && false);
	    String name = role.getGroup().getName().equalsIgnoreCase(m_eng.getString("~Integration")) ? role.getName() : m_eng.getString(role.getName());
	    row.appendData(name);
	    buffer.append("addGroupRole(").append(role.getId()).append(",'").append(role.getGroup().getName()).append("');\n");
	    return row;
	}

	/**
	 * Appends a new row representing a role to the specified array of role state snapshots
	 * @param row {@link SimpleSSRow} object representing the row
	 * @param svaRoleSSArr An array of {@link SimpleSS} objects representing role state snapshots
	 * @param role {@link IRole} object representing the role to be added as a new row.
	 * @throws Exception If an error occurs while appending row to SS array.
	 */
	private void appendRowToSSArray(SimpleSSRow row, SimpleSS[] svaRoleSSArr, IRole role) throws Exception {
	    IRoleGroup group = role.getGroup();
	    
	    int index = group.isAdmin() ? Lookup.LUV_YESNO_No : (svaRoleSSArr.length > Lookup.LUV_YESNO_Yes ? 1 : -1);
	    if(index != -1) {
	    	svaRoleSSArr[index].appendRow(role, row, role.getGroup());
	    }
	}

	/**
	 * Commits the role template using the provided role state snapshots and groups.
	 * @param svaRoleSSArr An array of {@link SimpleSS} objects representing role state snapshots.
	 * @param roleTemplateNode1 XML {@link Element} element representing the role template1 to commit.
	 * @param roleTemplateNode2 XML {@link Element} element representing the role template2 to commit.
	 * @param buffer {@link StringBuilder} used to construct the script for adding role groups.
	 * @param groups {@link Set} of {@link IRoleGroup} objects representing the role groups to be added.
	 * @throws Exception If an error occurs while committing role template.
	 */
	private void commitRoleTemplate(SimpleSS[] svaRoleSSArr, Element roleTemplateNode1, Element roleTemplateNode2, StringBuilder buffer, Set<IRoleGroup> groups) throws Exception {
		svaRoleSSArr[0].commit(roleTemplateNode1);
	    if(svaRoleSSArr.length > 1) {
	        svaRoleSSArr[1].commit(roleTemplateNode2);
	    }

	    Element script = m_eng.newElement(EZSCRIPT);
	    for(IRoleGroup group : groups) {
	        buffer.append("addGroup('").append(group.getName()).append("','").append(m_eng.getLocale().formatMessage(group.getDescription()));
	        buffer.append("',").append(group.getMin()).append(",").append(group.getMax()).append(");\n");
	    }

	    XMLHelper.setElementText(script, buffer.toString());
	    roleTemplateNode1.appendChild(script);
	}

	@Override
	public boolean onAnchor(AnchorEvent event) throws Exception {
		ToolbarComponent window = (ToolbarComponent) event.getContainer();
		createButtonAnchor(window, "addSVARoles", "~btnAdd", "addSVARoles()");
		createButtonAnchor(window, "removeSVARoles", "~lblRemove", "removeSVARoles()");
		createButtonAnchor(window, "close", "~ezAnchorCancel", "onCancel()");
		return true;
	}

	/**
	 * Creates an anchor button in the specified toolbar component
	 * @param window {@link ToolbarComponent} where the anchor button will be added.
	 * @param anchorId {@code String} identifier for the anchor button.
	 * @param name {@code String} the anchor name.
	 * @param url {@code String} the anchor URL
	 * @throws Exception if any error occurs during the creation of the anchor button
	 */
	private void createButtonAnchor(ToolbarComponent window, String anchorId, String name, String url) throws Exception {
		AnchorComponent anchorComponent = AnchorComponent.create(window, anchorId, m_eng.getString(name), url);
		anchorComponent.setAsButton(AnchorComponent.BUTTON);
	}
}