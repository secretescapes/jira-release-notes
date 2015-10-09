# Automated Jira Release Notes

# What is this?

Its a pretty simple gradle/groovy application. It pulls some information from JIRA and formats into a nicely formatted HTML template so your release notes look good, professional and consistent. The HTML template contains a bunch of inline CSS so that it can be viewed in Gmail and most other emails.

You can view a blog post about it here:
http://tech.secretescapes.com/2015/10/continuous-delivery/automating-release-notes/

# How does it work?

You pass in three parameters to the app and it produces a release-notes.html file.
* Jira URL: e.g. http://myjira.atlassian.net

* Base 64 encoded username and password in the format of <username password> - more info at https://developer.atlassian.com/jiradev/jira-apis/jira-rest-apis/jira-rest-api-tutorials/jira-rest-api-example-basic-authentication

* Release version number - e.g. 2.81

# How can I run it?

First of all you need to clone the repo and go into it. For those of you that don't know how to do it:
```
git clone git@github.com:secretescapes/jira-release-notes.git
cd jira-release-notes
```

There are two main ways to run it.

1. Run it through the gradle run command and pass in the three arguments like:

```
gradle run -PappArgs="['https://myjira.atlassian.net','JK41hbi532SdGVSA12GW==', '2.80']"
```

2. You can compile it into a jar as you would any other Gradle project and then just run it like you would any other other jar like:
 
``` 
java -jar jirareleasenotes-1.0.jar jiraurl base64encodedlogin 2.81
```

# Requirements

* Java 7_80
* Groovy 2.3.11
* Gradle 2.2
* Jira JIRA v7.1.0-OD-02-030

*Please note that it is very likely this will work with other versions, but has only been tested with the above.*

# Jira custom fields

We use two Jira custom fields that we have created ourselves. On each ticket we have a 'Release Version' field (customfield_10600) and a 'Assigned Team' field (customfield_10500). You will have to create these fields yourself in Jira and then make sure their customfield numbers match the ones in the code. Feel free to fork the project and update these values if they don't match.

Check here for more documentation on how to add custom fields in Jira:
https://confluence.atlassian.com/jira/adding-a-custom-field-185729521.html

# What can I do with this?

Once you get the generated HTML file you can do anything you want with this. Maybe host it on a server so people can view it as a URL, or just email it with an HTML Email client to people. 

What this was initially designed for however was integrating it with Jenkins so you can send release notes out via email with one click!

# What plugins do we use for Jenkins integration?

We used a few plugins:
* Email-ext - Used to send the email with the HTML template - https://wiki.jenkins-ci.org/display/JENKINS/Email-ext+plugin

* Workflow - Used to send the email to an approver first before sending off to everyone else - https://wiki.jenkins-ci.org/display/JENKINS/Workflow+Plugin
 
* Gradle - Used to run the app with the gradle run command - https://wiki.jenkins-ci.org/display/JENKINS/Gradle+Plugin

* Git - Used to pull app from Git repo - https://wiki.jenkins-ci.org/display/JENKINS/Git+Plugin 

# How did we integrate it with Jenkins?

1. We created a job that took in the 3 required params 

2. Pulled the app from the git repo using the Git plugin 

3. Used the gradle plugin to run it. Just filled the Tasks box with:
```
run -PappArgs="['$JiraURL', '$JiraEncoded64Login', '$ReleaseVersion']"

```

4. Lastly, sent the email to the team using the Email-ext plugin. We did this by going to Add post-build action -> Editable Email Notification. Then go to Advanced.. -> and fill in the Pre-send Script box with:
```
def reportPath = build.getWorkspace().child("release-notes.html")
msg.setContent(reportPath.readToString(), "text/html");
```

Optional - If you want to use the workflow plugin with the manual verification step, this is what our script looks like:
```
//1. Send email off to just one person to test it looks good
build job: 'Jira Automated Release Notes', parameters: [[$class: 'StringParameterValue', name: 'ReleaseVersion', value: "${ReleaseVersion}"], [$class: 'StringParameterValue', name: 'ReplyToList', value: "${InitialEmail}"], [$class: 'StringParameterValue', name: 'RecipientList', value: "${InitialEmail}"]]

//2. Wait for manual acceptance
input 'Check the release notes sent to your email? Does everything look ok? Once you hit proceed the notes will get sent to everyone!'

//3. Send off to everyone
build job: 'Jira Automated Release Notes', parameters: [[$class: 'StringParameterValue', name: 'ReleaseVersion', value: "${ReleaseVersion}"], [$class: 'StringParameterValue', name: 'RecipientList', value: 'bcc:everyone@example.com'], [$class: 'StringParameterValue', name: 'ReplyToList', value: 'tech@example.com, product@example.com']]
```

# License

This project is fully open source and licensed with the MIT License. In short, it means this is open-source and the only requirement is that if you re-use the project you release it under the MIT license. You can read the LICENSE.md file or read more about MIT licensing here: https://opensource.org/licenses/MIT
