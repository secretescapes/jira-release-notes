package com.secretescapes.jirereleasenotesauto

import groovy.json.JsonSlurper

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import groovyx.net.http.RESTClient

class JiraReleaseNotes {
	String jiraURL
	String encoded64AuthInfo
	RESTClient restClient

	public JiraReleaseNotes(String jiraURL, String encoded64AuthInfo) {
		this.jiraURL = jiraURL
		this.encoded64AuthInfo = encoded64AuthInfo

		restClient = new RESTClient(jiraURL)
		restClient.headers['Authorization'] = 'Basic '+ encoded64AuthInfo
		restClient.handler.failure = { resp -> println "ERROR: "+ resp.statusLine; return resp }
	}

	/**
	 *
	 * @param jiraURL - Jira URL: e.g. http://myjira.atlassian.net
	 * @param encoded64AuthInfo - Base 64 encoded username and password in the format of <username password> - more info @ https://developer.atlassian.com/jiradev/jira-apis/jira-rest-apis/jira-rest-api-tutorials/jira-rest-api-example-basic-authentication
	 * @param fixVersion - Release version number - e.g. 2.81
	 *
	 * @return ReleaseNotes object containing JiraTickets related to the fixVersion
	 */
	public ReleaseNotes getFixNotes(String fixVersion) {
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



	public void createFixVersionAndLinkWithTickets(String fixVersionToCreate, String issueIds) {
		createFixVersion(fixVersionToCreate)

		issueIds.split(",").each { issueId ->
		println "/rest/api/2/issue/${issueId.trim()}"

				def jsonObj =[	
				"update": [
					"fixVersions": [["add": 
						  ["name": fixVersionToCreate]
					]]
				  ]
				]
			
			
			def response  = restClient.put(
				path: "/rest/api/2/issue/${issueId.trim()}",
				contentType: ContentType.JSON,
				body: jsonObj)

		}
	}

	
	/**
	 *
	 */
	 private createFixVersion(String fixVersionToCreate) {
		def body =[	
				"name": fixVersionToCreate,
				"project": "DEV"
			]
		
		def resp = restClient.post(
			requestContentType: ContentType.JSON, 
			body: body,
			path: "/rest/api/2/version")
		if(resp?.status?.toString()?.startsWith("2")) {
			println "Version ${fixVersionToCreate} created. Response: ${resp?.statusLine}"
		} else if (resp?.status?.toString()?.startsWith("4")) {
			println "Version ${fixVersionToCreate} not created. Possibly exists already "
		} else {
			println "Version ${fixVersionToCreate} not created." + resp.properties
		}
	 }
	/**
	 * Updates JIRA issue with given ID with release notes provided as param
	 */
	 private updateReleaseNotes(String issueId, String releaseNotes) {
		println "/rest/api/2/issue/${issueId.trim()}"

			def jsonObj =[	
			"fields": 
				[
					"customfield_10600": releaseNotes
				]
			]
				
		def response  = restClient.put(
			path: "/rest/api/2/issue/${issueId.trim()}",
			contentType: ContentType.JSON,
			body: jsonObj)

		println "Notes for ${issueId} added."
		
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
