package giuaky;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

class Student{
	String id;
	String name;
	String address;
	String dateOfBirth;
	int age;
	int sum;
	boolean isDigit;
	
	public Student(String id, String name, String address, String dateOfBirth) {
		this.id = id;
		this.name = name;
		this.address = address;
		this.dateOfBirth = dateOfBirth;
	}

	public void setAge(int age) {
		this.age = age;
	}
	
	public void setSum(int sum) {
		this.sum = sum;
	}
	
	public void setisDigit(boolean isDigit) {
		this.isDigit = isDigit;
	}
	
	public Element toXmlElement(Document doc) {
	    Element studentElement = doc.createElement("student");

	    Element idElement = doc.createElement("id");
	    idElement.appendChild(doc.createTextNode(encrypt(id)));
	    studentElement.appendChild(idElement);

	    Element nameElement = doc.createElement("name");
	    nameElement.appendChild(doc.createTextNode(name));
	    studentElement.appendChild(nameElement);

	    Element addressElement = doc.createElement("address");
	    addressElement.appendChild(doc.createTextNode(address));
	    studentElement.appendChild(addressElement);

	    Element dobElement = doc.createElement("dateOfBirth");
	    dobElement.appendChild(doc.createTextNode(encrypt(dateOfBirth)));
	    studentElement.appendChild(dobElement);

	    Element ageElement = doc.createElement("age");
	    ageElement.appendChild(doc.createTextNode(Integer.toString(age)));
	    studentElement.appendChild(ageElement);

	    Element sumElement = doc.createElement("sum");
	    sumElement.appendChild(doc.createTextNode(Integer.toString(sum)));
	    studentElement.appendChild(sumElement);

	    Element digitElement = doc.createElement("isDigit");
	    digitElement.appendChild(doc.createTextNode(Boolean.toString(isDigit)));
	    studentElement.appendChild(digitElement);

	    return studentElement;
	}

	private String encrypt(String value) {
	    try {
	        Key key = new SecretKeySpec("encryptionkey123".getBytes(), "AES");
	        Cipher cipher = Cipher.getInstance("AES");
	        cipher.init(Cipher.ENCRYPT_MODE, key);
	        byte[] encryptedValue = cipher.doFinal(value.getBytes());
	        return Base64.getEncoder().encodeToString(encryptedValue);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}

	private String decrypt(String value) {
	    try {
	        Key key = new SecretKeySpec("encryptionkey123".getBytes(), "AES");
	        Cipher cipher = Cipher.getInstance("AES");
	        cipher.init(Cipher.DECRYPT_MODE, key);
	        byte[] decodedValue = Base64.getDecoder().decode(value);
	        byte[] decryptedValue = cipher.doFinal(decodedValue);
	        return new String(decryptedValue);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}	
	
	public String getId() {
        return decrypt(id);
    }

    public String getDateOfBirth() {
        return decrypt(dateOfBirth);
    }
}
	
class StudentReaderThread extends Thread {
    private String filename;
    private List<Student> students;

    public StudentReaderThread(String filename) {
        this.filename = filename;
        this.students = new ArrayList<>();
    }
    
    @Override
    public void run() {
        try {
            File xmlFile = new File(filename);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            NodeList studentList = doc.getElementsByTagName("student");

            for (int i = 0; i < studentList.getLength(); i++) {
                Element studentElement = (Element) studentList.item(i);
                String id = studentElement.getElementsByTagName("id").item(0).getTextContent();
                String name = studentElement.getElementsByTagName("name").item(0).getTextContent();
                String address = studentElement.getElementsByTagName("address").item(0).getTextContent();
                String dob = studentElement.getElementsByTagName("dateOfBirth").item(0).getTextContent();

                Student student = new Student(id, name, address, dob);
                students.add(student);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Student> getStudents() {
        return students;
    }
}
class AgeCalculatorThread extends Thread {
    private List<Student> students;

    public AgeCalculatorThread(List<Student> students) {
        this.students = students;
    }

    @Override
    public void run() {
        for (Student student : students) {
            String[] dobParts = student.dateOfBirth.split("-");
            int year = Integer.parseInt(dobParts[0]);
            int month = Integer.parseInt(dobParts[1]);
            int day = Integer.parseInt(dobParts[2]);
            int age = 2024 - year - ((4 > month || (4 == month && 10 >= day)) ? 0 : 1);
            student.setAge(age);
        }
    }
}

class DigitCheckerThread extends Thread {
    private List<Student> students;

    public DigitCheckerThread(List<Student> students) {
        this.students = students;
    }

    private boolean isDigit(int n) {
        if (n <= 1) {
            return false;
        }
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void run() {
        for (Student student : students) {
            int sum = 0;
            for (char c : student.getDateOfBirth().toCharArray()) {
                if (Character.isDigit(c)) {
                    sum += Character.getNumericValue(c);
                }
            }
            student.setSum(sum);
            student.setisDigit(isDigit(sum));
        }
    }
}


public class ktragiuaky {

	public static void main(String[] args) {
	    String filename = "students.xml";

	    StudentReaderThread readerThread = new StudentReaderThread(filename);
	    readerThread.start();

	    try {
	        readerThread.join();
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }

	    List<Student> students = readerThread.getStudents();

	    AgeCalculatorThread ageThread = new AgeCalculatorThread(students);
	    ageThread.start();

	    DigitCheckerThread digitThread = new DigitCheckerThread(students);
	    digitThread.start();

	    try {
	        ageThread.join();
	        digitThread.join();
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }

	    createResultXml(students);
	}

    private static void createResultXml(List<Student> students) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            Element rootElement = doc.createElement("students");
            doc.appendChild(rootElement);

            for (Student student : students) {
                Element studentElement = student.toXmlElement(doc);
                rootElement.appendChild(studentElement);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            FileOutputStream fos = new FileOutputStream("kq.xml");
            StreamResult result = new StreamResult(fos);

            transformer.transform(source, result);

            fos.close();
        } catch (ParserConfigurationException | TransformerException | IOException e) {
            e.printStackTrace();
        }
	}
}
