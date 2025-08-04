package com.ezdata.cdsportal.web.user.svauser;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a collection of SVA-enabled user roles.
 * @author Akash Shinde
 *
 * September 20, 2024 12:35:00 PM
 * Copyright Zinnia Distributor Solutions LLC 2024. All rights reserved.
 * ZINNIA PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
public class SVAEnabledUserRoles {

	/** A list of SVA-enabled user roles.*/
	@JsonProperty("svaEnabledUserRoles")
	private List<SVAEnabledUserRole> svaEnabledUserRoles = new ArrayList<>();

	/**
	 * Retrieves the list of SVA-enabled user roles.
	 * @return A list of {@link SVAEnabledUserRole} objects.
	 */
	public List<SVAEnabledUserRole> getSVAEnabledUserRoles() {
		return svaEnabledUserRoles;
	}

	/**
	 * Sets the list of SVA-enabled user roles.
	 * @param svaEnabledUserRoles An ArrayList of {@link SVAEnabledUserRole} objects to set.
	 */
	public void setSVAEnabledUserRoles(List<SVAEnabledUserRole> svaEnabledUserRoles) {
		this.svaEnabledUserRoles = svaEnabledUserRoles;
	}

	/** Represents a single SVA-enabled user role with a title and role Id.*/
	public static class SVAEnabledUserRole {

		/**The title of the role.*/
		private String roleTitle;

		/**The unique identifier of the role.*/
		private int roleId;

		/**
		 * Retrieves the title of the role.
		 * @return The title of the role.
		 */
		public String getRoleTitle() {
			return roleTitle;
		}

		/**
		 * Sets the title of the role.
		 * @param roleTitle The title of the role to set.
		 */
		public void setRoleTitle(String roleTitle) {
			this.roleTitle = roleTitle;
		}

		/**
		 * Retrieves the role Id.
		 * @return The unique identifier of the role.
		 */
		public int getRoleId() {
			return roleId;
		}

		/**
		 * Sets the role Id.
		 * @param roleId The unique identifier of the role to set.
		 */
		public void setRoleId(int roleId) {
			this.roleId = roleId;
		}
	}
}