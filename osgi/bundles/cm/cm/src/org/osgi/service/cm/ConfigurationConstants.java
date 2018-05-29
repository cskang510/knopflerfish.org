/*
 * Copyright (c) OSGi Alliance (2017). All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.osgi.service.cm;


/**
 * Defines standard constants for the Configuration Admin service.
 * 
 * @author $Id: 3e96b9e44edc7110ecc4e373a0d4cb2930098718 $
 */
public final class ConfigurationConstants {
	private ConfigurationConstants() {
		// non-instantiable
	}


	/**
	 * The name of the implementation capability for the Configuration Admin
	 * specification
	 * 
	 * @since 1.6
	 */
	public static final String	CONFIGURATION_ADMIN_IMPLEMENTATION			= "osgi.cm";

	/**
	 * The version of the implementation capability for the Configuration Admin
	 * specification
	 * 
	 * @since 1.6
	 */
	public static final String	CONFIGURATION_ADMIN_SPECIFICATION_VERSION	= "1.6.0";
}