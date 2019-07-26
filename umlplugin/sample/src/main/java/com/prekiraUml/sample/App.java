package com.prekiraUml.sample;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import net.sourceforge.plantuml.GeneratedImage;
import net.sourceforge.plantuml.SourceFileReader;

/**
 * Takes dtomap json and turns into txt file for plant uml to visualize
 */
public class App {
	public static String[] stereotypes = { "Audited", "interface", "enumeration" };
	// make into map
	public static ArrayList<JSONObject> enumList = new ArrayList<JSONObject>();
	public static ArrayList<String> enumNames = new ArrayList<String>();
	public static String connectionsUniversal = "";
	//todo: move function output to this variable
	public static String classInfo = "";


	public static void createDiag(final String path) throws IOException, InterruptedException {
		// get dto data, create text for plantuml
		JSONArray dtoMap = parseJson(path).getJSONArray("data");
		String umlText = "@startuml\n" + getStyleParams(dtoMap) + getClassInfo(dtoMap) + getEnumInfo() + connectionsUniversal + "@enduml";

		PrintStream o = new PrintStream(new File("DtoMapPlantUml.txt"));
		// Store current System.out before assigning a new value
		PrintStream console = System.out;
		// Assign o to output stream
		System.setOut(o);
		// write to file
		print(umlText);
		// set output stream to console post writing to file
		System.setOut(console);

		print("complete");
		
		// Return a null string if no generation
        /*
         * can cause issues with graphviz dependency, 
         * use command line java -jar plantuml.jar DtoMapPlantUml.txt instead
		 * */
        //generating image
        File source = new File("DtoMapPlantUml.txt");
        SourceFileReader reader = new SourceFileReader(source);
        java.util.List<GeneratedImage> list = reader.getGeneratedImages();
        // Generated files
        File png = list.get(0).getPngFile();
        

    }
	

	/**
	 * goes through list of classes and obtains property and connection information
	 * @param dtoMap containing list of classes
	 * @return text for plantuml to visualize
	 */
	public static String getClassInfo(JSONArray dtoMap) {
		String umlText = "";
		for (int i = 0; i < dtoMap.length(); i++) {
			if (dtoMap.getJSONObject(i).getString("classPackage").indexOf("java") == -1 && 
			!(dtoMap.getJSONObject(i).getString("className").equalsIgnoreCase("dto"))) {
				umlText += "class " + (dtoMap.getJSONObject(i).get("className")); 
                umlText += getStereotypeFromDto(dtoMap.getJSONObject(i));
                umlText += getPropertiesFromDto(dtoMap.getJSONObject(i));
                umlText += "\n";
            }
        }
		return umlText;
	} 
	
	/**
	 * goes through list of enums found and obtains information
	 * @return text for plantuml to visualize
	 */
	public static String getEnumInfo() {
		String umlText = "";
		for (int i = 0; i < enumList.size(); i++) {
            umlText += "enum " + getClassNameFromString(enumList.get(i).getJSONObject("value").getString("name")) + " {\n";
            JSONArray enumValueList = enumList.get(i).getJSONObject("value").getJSONArray("values");
            for (int j = 0; j < enumValueList.length(); j++) {
            	umlText += "   " + enumValueList.getJSONObject(j).getString("name").toLowerCase() + "\n";
            }
                
            umlText += "}\n";
        	
        }
		return umlText;
	}
	
	/**
	 * provides styling, number of pages for diagrams
	 * @param dtoList list of dtos
	 * @return styling text
	 */
	public static String getStyleParams(JSONArray dtoList) {
		//styling
		String umlText = "skinparam class {\n    BackgroundColor PaleGreen\n     ArrowColor SeaGreen\n   orderColor SpringGreen\n}\n";
        umlText += "skinparam enum {\n    BackgroundColor PaleBlue\n     ArrowColor Cyan\n   orderColor Blue\n}\n";

		//split into pages if large:
        int dimension = (int) (Math.max(Math.pow(dtoList.length()/20, 0.5), 1));
        return umlText + "\npage "+ dimension + "x" + dimension + "\n";
	}
	
	/**
	 * Get type, name, if enum, optional, cardinality, inter-class, embedded class connections
	 * @param dto current dto to get properties for
	 * @return text for plant uml to visualize 
	 */
	public static String getPropertiesFromDto(JSONObject dto) {
		String propertyOfClass = "{\n";
		String connectionsOfClass = "";
		String embeddedClasses = "";
		JSONArray propertyList = dto.getJSONArray("properties");
		for (int i = 0; i < propertyList.length(); i++) {
			propertyOfClass += "	+";
			
			//type of field
			propertyOfClass += getClassNameFromString(propertyList.getJSONObject(i).getString("type"));
			
			
			//name of field
			propertyOfClass += " " + propertyList.getJSONObject(i).getString("name") + ": ";
			
			//if enum, and not duplicate enum
			//TODO: conv to map
			if (propertyList.getJSONObject(i).getJSONObject("dtoEnums").has("value") ) {
				JSONObject enumCurrent = propertyList.getJSONObject(i).getJSONObject("dtoEnums");
				if (!(enumNames.contains(getClassNameFromString(enumCurrent.getJSONObject("value").getString("name"))))) {
					enumList.add(enumCurrent);
					String enumName = getClassNameFromString(enumCurrent.getJSONObject("value").getString("name"));
					enumNames.add(enumName);

					//EDGE case when type is .DTO, so have to capitalize name of enum to get connection
					enumName = enumName.substring(0, 1).toUpperCase() + enumName.substring(1);
					connectionsUniversal += "\n " + enumName + " ----* " + getClassNameFromString(dto.getString("className")) + " : " + enumName + " (inner class)\n";
					
				}
			}
			propertyOfClass += embeddedClasses;
			//if field has "Optional" or "not required" annotation
			boolean isRequired = true;
			if ((propertyList.getJSONObject(i).getJSONObject("annotations").has("notRequired") && 
					propertyList.getJSONObject(i).getJSONObject("annotations").getString("notRequired").contentEquals("true")) ||
					(propertyList.getJSONObject(i).getJSONObject("annotations").has("Optional") && 
					propertyList.getJSONObject(i).getJSONObject("annotations").getString("Optional").contentEquals("true"))) {
				isRequired = false;
				propertyOfClass += "(Optional)";
			}
			
			propertyOfClass += "\n";
			
			//cardinality of class
			//doubled bc current DtoMaps have both upper and lowercase
			String cardinality = "";
			if (propertyList.getJSONObject(i).getJSONObject("annotations").has("swaggerReference") || 
					propertyList.getJSONObject(i).getJSONObject("annotations").has("SwaggerReference")) {
				if (propertyList.getJSONObject(i).getJSONObject("annotations").has("isList") &&
						propertyList.getJSONObject(i).getJSONObject("annotations").getString("isList").contentEquals("true")) {
					//using 0''* instead of .. because leads to plant uml syntax errors
					cardinality = "0''*";
					//
				} else {
					if (isRequired) {
						cardinality = "1";
					} else {
						cardinality = "0''1";
					}
				}
			}

			//connections between classes
			//double calls because current DtoMaps have both upper and lowercase
			if (propertyList.getJSONObject(i).getJSONObject("annotations").has("swaggerReference")) {
				connectionsOfClass += "\n" + getClassNameFromString(propertyList.getJSONObject(i).getJSONObject("annotations").getString("swaggerReference") + 
					" --o " + dto.getString("className") + ": " + propertyList.getJSONObject(i).getString("name") + "	" + cardinality + "\n");
			} else if (propertyList.getJSONObject(i).getJSONObject("annotations").has("SwaggerReference")) {
				connectionsOfClass += "\n" + getClassNameFromString(propertyList.getJSONObject(i).getJSONObject("annotations").getString("SwaggerReference") + 
					" --o " + dto.getString("className") + ": " + propertyList.getJSONObject(i).getString("name") + "	" + cardinality + "\n");
					
			} else {
				//do nothing, here for testing purposes
			}
			
			//find embedded class and connection between inner and outer classes
			if (isDto(propertyList.getJSONObject(i).getString("dtoClassName"))) {
				//inner class -> outer class : inner class
				connectionsOfClass += "\n "  + getClassNameFromString(propertyList.getJSONObject(i).getString("dtoClassName"))+ " -*" + 
						dto.getString("className") + " : + " + getClassNameFromString(propertyList.getJSONObject(i).getString("dtoClassName")) + " (inner class) \n";
					
			}
			print(embeddedClasses);
		}
		return propertyOfClass + "}" + connectionsOfClass + "\n" + embeddedClasses;
	}
	
	/**
	 * find if dtoobject is dto from type name
	 * @param typeName full type name
	 * @return if obj is dto
	 */
	public static boolean isDto(String typeName) {
		return typeName.substring(typeName.lastIndexOf(".") - "dto".length(), typeName.lastIndexOf(".")).toLowerCase().equals("dto");
		
	}
	
	/**
	 * extract class name from string of package directory
	 * @param name of path
	 * @return name of individual class
	 */
	public static String getClassNameFromString(String name) {
		return name.substring(name.lastIndexOf(".")+1, name.length()); 
	}
    
	/**
	 * stereotyping for dto by going through possible stereotypes and seeing if
	 * contained in dto annotationsmv
	 * @param dto to stereotype
	 * @return formatted string for plant uml to signify stereotype
	 */
	public static String getStereotypeFromDto(JSONObject dto) {
		for (int i = 0; i < stereotypes.length; i++) {
			if (((JSONObject) dto.get("annotations")).has(stereotypes[i]) && ((JSONObject) dto.get("annotations")).getString(stereotypes[i]).equals("true")) {
				return " << " + stereotypes[i] + " >> ";
			}
		}
		return "";
	}
	
    /*easier printing syntax*/
    public static void print(Object x) {
    	System.out.println(x.toString());
    }
    
    /**
     * parse json from file 
     * @return parsed json object containing dto info
     */
    public static JSONObject parseJson(String path) {
		//String[] newPath = path.split("/target");
		
    	//String projectJsonPath = newPath[0] + "/api" + "/target" +newPath[1] + "/generated-sources/generated-dto/DtoMap.json";
		//print(projectJsonPath);
    	int ver = 1;
        String jsonPath = path + "DtoMap.json";
        return new JSONObject("{ \"data\": " + Data.getFileContentsAsString(jsonPath) + "}");
	}
	
	/**last resort to get access */
	public static String removeDuplicates(String originalPath) {
		String[] dirs = originalPath.split("/");
		String newPath = "";
		String previousDir = "";
		for (int i = 0; i < dirs.length; i++) {
			if (!(dirs[i].equals(previousDir))) {
				newPath += dirs[i] + "/";
			}
			previousDir = dirs[i];
		}
		return newPath;
	}

}
