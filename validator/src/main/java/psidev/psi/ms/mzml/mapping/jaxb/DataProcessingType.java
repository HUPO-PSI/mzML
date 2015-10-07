//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.1-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.08.17 at 01:20:51 PM BST 
//


package psidev.psi.ms.mzml.mapping.jaxb;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayInputStream;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;


/**
 * Description of the software, and the way in which it was used to generate the peak list.
 * 
 * <p>Java class for DataProcessingType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DataProcessingType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="processingMethod" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://psi.hupo.org/schema_revision/mzML_0.93}ParamGroupType">
 *                 &lt;attribute name="order" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *               &lt;/extension>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="softwareRef" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataProcessingType", propOrder = {
    "processingMethod"
})
public class DataProcessingType {

    @XmlElement(required = true)
    protected List<DataProcessingType.ProcessingMethod> processingMethod;
    @XmlAttribute(required = true)
    protected String id;
    @XmlAttribute(required = true)
    protected String softwareRef;


    private String elementName = "dataProcessing";

    public DataProcessingType() {}

    public DataProcessingType(String xmlSnippet) {
        parseXml(xmlSnippet);
    }

    public DataProcessingType(Element element) {
        create(element);
    }

    ///////////////////
    // utilities

    private void parseXml(String xmlSnippet) {
        Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            ByteArrayInputStream str = new ByteArrayInputStream(xmlSnippet.getBytes());
            document = builder.parse( str );
        } catch ( Exception e ) {
            e.printStackTrace();
        }


        // we always expect a String representing a single xml tag, not a list of xml tags!
        if (document != null) {
            NodeList nl = document.getElementsByTagName(elementName);
            if (nl.getLength() == 1) {
                Element ele = (Element) nl.item(0);
                create(ele);
            } else {
                throw new IllegalStateException("Expected only one '" + elementName + "' element, but found: " + nl.getLength());
            }
        }

    }

    private void create(Node node) {
        Element ele = (Element) node;
        if (ele != null) {
            // load attributes
            String id = ele.getAttribute("id");
            if (!id.equals("")) { // getAttribute returns empty String if no value was specified
                setId(id);
            }
            String softwareRef = ele.getAttribute("softwareRef");
            if (!softwareRef.equals("")) { // getAttribute returns empty String if no value was specified
                setSoftwareRef(softwareRef);
            }
            // load elements
            NodeList nl = ele.getElementsByTagName("processingMethod");
            for ( int i = 0; i < nl.getLength(); i++ ) {
                this.getProcessingMethod().add(new ProcessingMethod((Element) nl.item(i)));
            }


        }
    }

    ////////////////////
    // Getter + Setter

    /**
     * Gets the value of the processingMethod property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the processingMethod property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProcessingMethod().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataProcessingType.ProcessingMethod }
     * 
     * 
     */
    public List<DataProcessingType.ProcessingMethod> getProcessingMethod() {
        if (processingMethod == null) {
            processingMethod = new ArrayList<DataProcessingType.ProcessingMethod>();
        }
        return this.processingMethod;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the softwareRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSoftwareRef() {
        return softwareRef;
    }

    /**
     * Sets the value of the softwareRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSoftwareRef(String value) {
        this.softwareRef = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://psi.hupo.org/schema_revision/mzML_0.93}ParamGroupType">
     *       &lt;attribute name="order" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class ProcessingMethod
        extends ParamGroupType
    {

        @XmlAttribute
        protected BigInteger order;


        private String elementName = "processingMethod";

        public ProcessingMethod() {}

        public ProcessingMethod(String xmlSnippet) {
            parseXml(xmlSnippet);
        }

        public ProcessingMethod(Element element) {
            create(element);
        }

        ///////////////////
        // utilities

        private void parseXml(String xmlSnippet) {
            Document document = null;
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                ByteArrayInputStream str = new ByteArrayInputStream(xmlSnippet.getBytes());
                document = builder.parse( str );
            } catch ( Exception e ) {
                e.printStackTrace();
            }


            // we always expect a String representing a single xml tag, not a list of xml tags!
            if (document != null) {
                NodeList nl = document.getElementsByTagName(elementName);
                if (nl.getLength() == 1) {
                    Element ele = (Element) nl.item(0);
                    create(ele);
                } else {
                    throw new IllegalStateException("Expected only one '" + elementName + "' element, but found: " + nl.getLength());
                }
            }

        }

        protected void create(Node node) {
            Element ele = (Element) node;
            if (ele != null) {
                // load attributes
                String order = ele.getAttribute("order");
                if (!order.equals("")) { // getAttribute returns empty String if no value was specified
                    setOrder(new BigInteger(order));
                }
                // load elements
                // inherited form ParamGroupType
                super.create(node);
            }
        }

        ////////////////////
        // Getter + Setter

        /**
         * Gets the value of the order property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getOrder() {
            return order;
        }

        /**
         * Sets the value of the order property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setOrder(BigInteger value) {
            this.order = value;
        }

    }

}
