package org.folio.gobi;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.folio.rest.mappings.model.Mapping.Field;
import org.folio.gobi.DataSource.Builder;
import org.folio.gobi.Mapper.Translation;
import org.folio.rest.impl.PostGobiOrdersHelper;
import org.folio.rest.mappings.model.DataSource;
import org.folio.rest.mappings.model.Mapping;
import org.folio.rest.mappings.model.Mappings;
import org.folio.rest.mappings.model.OrderMapping;
import org.folio.rest.mappings.model.OrderMapping.OrderType;
import org.folio.rest.mappings.model.Mapping;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class MappingHelper {
	private static final Logger logger = Logger.getLogger(MappingHelper.class);
	
	private static final String path = "data-mapping.json";
	
	
	
	 public static void defaultMapping() {
		 
	 	Map<OrderType,Map<Mapping.Field, org.folio.gobi.DataSource>> defaultMapping = 
				  new LinkedHashMap<>();
		
		  String jsonAsString = readMappingsFile(path);
		  final Mappings mappings = Json.decodeValue(jsonAsString, Mappings.class);
		  final List<OrderMapping> orderMappingList = mappings.getOrderMappings();	//get orderMappings list
		  for (OrderMapping orderMapping : orderMappingList) {						//iterate through orderMappings list
			  Map<Mapping.Field, org.folio.gobi.DataSource> fieldDataSourceMapping = new LinkedHashMap<>();
			  OrderType orderType = orderMapping.getOrderType();					//get orderType from orderMapping
			  List<Mapping> mappingsList = orderMapping.getMappings();				//get mappings list
			  for(int i=0;i< mappingsList.size();i++) {								//iterate 
				  Mapping mapping = mappingsList.get(i);							//get mapping
				  Mapping.Field field = mapping.getField();									//get field
				  org.folio.gobi.DataSource dataSource = getDS(mapping, fieldDataSourceMapping);
				  //get datasource
				  //String dataSourceFrom = dataSource.getFrom();
				  //String dataSourceDefault = dataSource.getDefault();
				  fieldDataSourceMapping.put(field, dataSource);
				  
				  //int x=1;
			  }
			  defaultMapping.put(orderType, fieldDataSourceMapping);
		  }
		  //logger.info(mappingList);
		  logger.info(mappings.toString());
	  }

	 public static org.folio.gobi.DataSource getDS(Mapping mapping, Map<Field, org.folio.gobi.DataSource> fieldDataSourceMapping) {
		 org.folio.gobi.DataSource ds;
		 
		 Object defaultValue = new Object();
		 if(mapping.getDataSource().getDefault() != null) {
			 defaultValue = mapping.getDataSource().getDefault();
		 }
		 else if(mapping.getDataSource().getFromOtherField() != null) {
			 String otherField = mapping.getDataSource().getFromOtherField().value();
			 defaultValue = fieldDataSourceMapping.get(Field.valueOf(otherField));
		 }
		 else if(mapping.getDataSource().getDefaultMapping() != null) {
		      defaultValue = getDS(mapping.getDataSource().getDefaultMapping(), fieldDataSourceMapping);
		    }
		 
		 String dataSourceFrom = mapping.getDataSource().getFrom();
		 
		 org.folio.rest.mappings.model.DataSource.Translation translation = mapping.getDataSource().getTranslation();
		 Translation<?> t = null;
		 if (translation != null) {
			 try {
				 Method translationMethod = Mapping.class.getMethod(translation.toString(), String.class);
				 t = data -> {
					 try {
						 return (CompletableFuture<Object>) translationMethod.invoke(null, data);
					 }
					 catch (Exception e) {
						 logger.error("Unable to invoke translation method: " + translation, e);
					 }
					 return null;
				 };
			 }
			 catch(NoSuchMethodException e) {
			    logger.error("Translation method not found: " + translation, e);
			 }
		 }
		 ds = org.folio.gobi.DataSource.builder().withFrom(dataSourceFrom).withDefault(defaultValue).withTranslation(t).build();
		 
		 return ds;
	 }
	 
	  public static String readMappingsFile(final String path) {
		  try {
			  final InputStream is = PostGobiOrdersHelper.class.getClassLoader().getResourceAsStream(path);
			  if (is != null) {
			        return IOUtils.toString(is, "UTF-8");
			  } else {
			        return "";
			  }
		  }
		  catch (Throwable e) {
		      logger.error(String.format("Unable to read mock configuration in %s file", path));
		    }
		  return "";
	  }
}
