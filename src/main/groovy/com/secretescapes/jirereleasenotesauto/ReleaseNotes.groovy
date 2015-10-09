package com.secretescapes.jirereleasenotesauto

import groovy.transform.ToString

/**
 * Container class that can hold multiple Jira Tickets.
 */
@ToString
class ReleaseNotes {

	String version
	List<JiraTicket> releaseIssues = []
}
