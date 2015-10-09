package com.secretescapes.jirereleasenotesauto

import groovy.transform.ToString

/**
 * Created by dean on 09/10/15.
 */
@ToString
class ReleaseNotes {

	String version
	List<ReleaseNotes> releaseNotes = []
}
