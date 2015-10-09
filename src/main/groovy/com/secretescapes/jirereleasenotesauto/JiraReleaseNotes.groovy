package com.secretescapes.jirereleasenotesauto

import groovy.json.JsonSlurper

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

/**
 * Created by dean on 25/09/15.
 */
class JiraReleaseNotes {

	public static final String JIRA_URL = "https://***REMOVED***"

	public ReleaseNotes getFixNotes(String encoded64AuthInfo, String fixVersion) {
		def http = new HTTPBuilder(JIRA_URL)
		//http.ignoreSSLIssues()

		def results

		http.request(Method.GET, ContentType.TEXT) { req ->

			uri.path = "/rest/api/2/search"
			uri.query = [jql : "fixVersion=${fixVersion}", fields: "key,summary,customfield_10600" ]
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
			ReleaseIssue releaseIssue = new ReleaseIssue(id: issue.id, key: issue.key, link: issue.self, summary: issue.fields.summary, note: issue.fields.customfield_10600)
			releaseNotes.releaseNotes.add(releaseIssue)
		}
		releaseNotes
	}

	public String formatAsHtml(ReleaseNotes releaseNotes) {
		def releaseNotesAsHtml = ""
		releaseNotesAsHtml += "<h1>Release Notes for ${releaseNotes.version}</h1>\n"
		releaseNotes.each { releaseNote ->
			releaseNotesAsHtml += releaseNote.toString() + "\n"
		}
		releaseNotesAsHtml += "For more info contact XXXXX"
	}

}
