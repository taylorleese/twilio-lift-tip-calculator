package bootstrap.liftweb

import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.http._
import _root_.net.liftweb.http.provider._
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import _root_.net.liftweb.util.Helpers
import com.tipcalc.rest.TipRest

class Boot {
  def boot {
    // setup the doc type
    LiftRules.docType.default.set((r: Req) => r match {
      case _ if S.skipDocType => Empty
      case _ if S.getDocType._1 => S.getDocType._2
      case _ => Full(DocType.html5)
    })
	  
    // setup rest api
    LiftRules.dispatch.append(TipRest)
    
    // where to search snippet
    LiftRules.addToPackages("com.tipcalc")

    // build sitemap
    def sitemap() = SiteMap(
      Menu("Home") / "index"
    )
    
    // configure sitemap
    LiftRules.setSiteMap(sitemap())
    
    // show the spinny image when an ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)
    
    // make utf-8
    LiftRules.early.append(r => r.setCharacterEncoding("UTF-8"))
    
    // use html5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))
  }
}
