package org.optaplanner.examples.vehiclerouting.app;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.examples.vehiclerouting.domain.Customer;
import org.optaplanner.examples.vehiclerouting.domain.Vehicle;
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution;
import org.optaplanner.persistence.xstream.impl.domain.solution.XStreamSolutionFileIO;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class VehicleRoutingSolutionApp {
    private VehicleRoutingSolution readFromString(String xml) {
        XStreamSolutionFileIO xStreamSolutionFileIO = new XStreamSolutionFileIO(VehicleRoutingSolution.class);
        return (VehicleRoutingSolution) xStreamSolutionFileIO.getXStream().fromXML(xml);
    }

    public String solve(String str) {
        SolverFactory<VehicleRoutingSolution> solverFactory = SolverFactory.createFromXmlResource("org/optaplanner/examples/vehiclerouting/solver/vehicleRoutingSolverConfig.xml");
        Solver<VehicleRoutingSolution> solver = solverFactory.buildSolver();

        VehicleRoutingSolution solution = readFromString(str);

        solver.solve(solution);

        VehicleRoutingSolution bestSolution = solver.getBestSolution();
        return getXml(bestSolution.getVehicleList());
    }

    private void getCustomers(List<Customer> customers, Customer customer) {
        if (customer != null) {
            if (customer.getNextCustomer() == null)
                customers.add(customer);
            else {
                customers.add(customer);
                getCustomers(customers, customer.getNextCustomer());
            }
        }
    }

    public String test(String s) {
        return s + " test";
    }

    public static String staticTest(String s) {
        return s + " static test";
    }

    public static void main(String[] args) {
    }

    private String getXml(List<Vehicle> vehicleList) {
        Document dom;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.newDocument();

            Element rootEle = dom.createElement("routing");

            for (Vehicle vehicle : vehicleList) {
                Element veh = dom.createElement("vehicle");
                veh.setAttribute("name", vehicle.toString());

                List<Customer> customers = new ArrayList<>();
                getCustomers(customers, vehicle.getNextCustomer());
                int sequence = 0;

                for (Customer customer : customers) {
                    Element cust = dom.createElement("customer"),
                            id = dom.createElement("id"),
                            seq = dom.createElement("sequence");

                    id.appendChild(dom.createTextNode(customer.toString()));
                    seq.appendChild(dom.createTextNode(String.valueOf(++sequence)));

                    cust.appendChild(id);
                    cust.appendChild(seq);

                    veh.appendChild(cust);
                }

                rootEle.appendChild(veh);
            }

            dom.appendChild(rootEle);

            try {
                StringWriter writer = new StringWriter(0);
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

                tr.transform(new DOMSource(dom), new StreamResult(writer));

                return writer.toString();

            } catch (TransformerException te) {
                System.out.println(te.getMessage());
            }
        } catch (ParserConfigurationException pce) {
            System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
        }

        return "";
    }
}
