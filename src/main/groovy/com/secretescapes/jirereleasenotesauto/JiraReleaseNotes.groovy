package com.secretescapes.jirereleasenotesauto

import groovy.json.JsonSlurper

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

class JiraReleaseNotes {

	/**
	 *
	 * @param jiraURL - Jira URL: e.g. http://myjira.atlassian.net
	 * @param encoded64AuthInfo - Base 64 encoded username and password in the format of <username password> - more info @ https://developer.atlassian.com/jiradev/jira-apis/jira-rest-apis/jira-rest-api-tutorials/jira-rest-api-example-basic-authentication
	 * @param fixVersion - Release version number - e.g. 2.81
	 *
	 * @return ReleaseNotes object containing JiraTickets related to the fixVersion
	 */
	public ReleaseNotes getFixNotes(String jiraURL, String encoded64AuthInfo, String fixVersion) {
		def http = new HTTPBuilder(jiraURL)
		//http.ignoreSSLIssues() -> Only uncomment if having SSL issues

		def results

		http.request(Method.GET, ContentType.TEXT) { req ->

			uri.path = "/rest/api/2/search"
			uri.query = [jql : "fixVersion=${fixVersion}"/*, fields: "key,summary,customfield_10600"*/ ]
			headers.Accept = 'application/json'
			headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'
			headers.'Authorization' = "Basic ${encoded64AuthInfo}"

			response.success = { resp, reader ->
				assert resp.statusLine.statusCode == 200
				println "Got response: ${resp.statusLine}"
				println "Content-Type: ${resp.headers.'Content-Type'}"
				results = reader.text
			}

			response.'404' = {
				println 'Not found'
			}
		}

		def jsonSlurper = new JsonSlurper()
		def json = jsonSlurper.parseText(results)

		ReleaseNotes releaseNotes = new ReleaseNotes(version: fixVersion)
		json.issues.each{ issue ->
			JiraTicket releaseIssue = new JiraTicket(id: issue.id, key: issue.key, apiLink: issue.self, summary: issue.fields?.summary,
					note: issue.fields?.customfield_10600, team: issue.fields?.customfield_10500?.value?:"No team assigned", link: "${jiraURL}/browse/${issue.key}")
			releaseNotes.releaseIssues.add(releaseIssue)
		}
		releaseNotes
	}

	/**
	 *
	 * @param releaseNotes
	 * @return String containing full HTML template of release notes
	 */
	public String formatAsHtml(ReleaseNotes releaseNotes) {
		def releaseNotesAsHtml = ""
		releaseNotesAsHtml += HtmlTemplate.start(releaseNotes.version)
		releaseNotes.releaseIssues.each { issue ->
			releaseNotesAsHtml += HtmlTemplate.issueSummary(issue)
		}
		releaseNotes.releaseIssues.each { issue ->
			releaseNotesAsHtml += HtmlTemplate.releaseNote(issue)
		}
		releaseNotesAsHtml += HtmlTemplate.end()
	}

}
