package com.company;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.company.dto.BarCodesA;
import com.company.dto.BarCodesB;
import com.company.dto.CatalogA;
import com.company.dto.CatalogB;
import com.company.dto.OutputProduct;
import com.company.dto.SuppliersA;
import com.company.dto.SuppliersB;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

@SpringBootApplication
public class CompanyApplication {

	public static void main(String[] args)
			throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		{

			String barcodesA = "src\\main\\resources\\barcodesA.csv";
			String barcodesB = "src\\main\\resources\\barcodesB.csv";
			String catalogA = "src\\main\\resources\\catalogA.csv";
			String catalogB = "src\\main\\resources\\catalogB.csv";
			String suppliersA = "src\\main\\resources\\suppliersA.csv";
			String suppliersB = "src\\main\\resources\\suppliersB.csv";

			// Reading the CSV file and convert into Java object
			List<BarCodesA> barcodeAList = getBarCodesA(barcodesA);
			List<BarCodesB> barcodeBList = getBarCodesB(barcodesB);
			List<CatalogA> catalogAList = getCatalogA(catalogA);
			List<CatalogB> catalogBList = getCatalogB(catalogB);
			List<SuppliersA> suppliersAList = getSuppliersA(suppliersA);
			List<SuppliersB> suppliersBList = getSuppliersB(suppliersB);
			System.out.println(barcodeAList);
			System.out.println(barcodeBList);
			System.out.println(catalogAList);
			System.out.println(catalogBList);
			System.out.println(suppliersAList);
			System.out.println(suppliersBList);

			List<OutputProduct> outputProductList = new ArrayList<>();

			// if there are unique products in Company B we can directly add to company A
			List<String> catlogaSKUs = catalogAList.stream().map(CatalogA::getSku).collect(Collectors.toList());
			List<String> catlogbSKUs = catalogBList.stream().map(CatalogB::getSku).collect(Collectors.toList());
			catlogaSKUs.removeAll(catlogbSKUs);
			catalogBList.stream().filter(cb -> !catlogbSKUs.contains(cb.getSku())).forEach(e -> {
				OutputProduct op = new OutputProduct();
				op.setDescription(e.getDescription());
				op.setSku(e.getSku());
				op.setSource("A");
				outputProductList.add(op);
			});

			Map<String, List<String>> barCodesAmap = barcodeAList.stream().collect(Collectors
					.groupingBy(BarCodesA::getSku, Collectors.mapping(BarCodesA::getBarcode, Collectors.toList())));
			System.out.println(barCodesAmap.toString());
			Map<String, List<String>> barCodesBmap = barcodeBList.stream().collect(Collectors
					.groupingBy(BarCodesB::getSku, Collectors.mapping(BarCodesB::getBarcode, Collectors.toList())));
			System.out.println(barCodesBmap.toString());

			// if cat A barcodes and cat B barcodes are not matched then we can move that
			// product to company A
			catalogAList.stream().forEach(ca -> {
				catalogBList.stream().forEach(cb -> {
					if (barCodesAmap.containsKey(ca.getSku()) && barCodesBmap.containsKey(cb.getSku())) {
						if (barCodesAmap.get(ca.getSku()) != null) {
							List<String> aBarcodes = barCodesAmap.get(ca.getSku());
							List<String> bBarcodes = barCodesBmap.get(cb.getSku());
							if (!StringUtils.equalsIgnoreCase(ca.getSku(), cb.getSku())
									&& !aBarcodes.containsAll(bBarcodes)) {
								OutputProduct op = new OutputProduct();
								op.setDescription(ca.getDescription());
								op.setSku(ca.getSku());
								op.setSource("A");
								outputProductList.add(op);
							} else {
								OutputProduct op = new OutputProduct();
								op.setDescription(cb.getDescription());
								op.setSku(cb.getSku());
								op.setSource("B");
								outputProductList.add(op);
							}
						}
					}
				});
			});

			List<OutputProduct> outputList = outputProductList.stream().filter(distinctByKey(p -> p.getSku()))
					.collect(Collectors.toList());
			// remove the 1st row as it contains column info
			outputList.remove(0);

			// covert Java object and generate CSV file
			System.out.println(outputList);
			generateCSVfile(outputList);

		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void generateCSVfile(List<OutputProduct> outputList)
			throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		Writer writer = Files.newBufferedWriter(Paths.get("src\\main\\resources\\output.csv"));

		ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
		mappingStrategy.setType(OutputProduct.class);

		// Arrange column name as provided in below array.
		String[] columns = new String[] { "SKU", "Description", "Source" };
		mappingStrategy.setColumnMapping(columns);

		// Creating StatefulBeanToCsv object
		StatefulBeanToCsvBuilder<OutputProduct> builder = new StatefulBeanToCsvBuilder(writer);

		StatefulBeanToCsv beanWriter = builder.withMappingStrategy(mappingStrategy).build();

		// Write list to StatefulBeanToCsv object
		beanWriter.write(outputList);

		// closing the writer object
		writer.close();
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> map = new ConcurrentHashMap<>();
		return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	private static List<SuppliersB> getSuppliersB(String suppliersBFile) throws FileNotFoundException, IOException {
		List<SuppliersB> suppliersBList = new ArrayList<>();
		try (CSVReader reader = new CSVReader(new FileReader(suppliersBFile))) {
			List<String[]> r = reader.readAll();
			r.forEach(e -> {
				SuppliersB suppliersB = new SuppliersB();
				suppliersB.setId(e[0]);
				suppliersB.setName(e[1]);
				suppliersBList.add(suppliersB);
			});
		}
		return suppliersBList;
	}

	private static List<SuppliersA> getSuppliersA(String suppliersAFile) throws FileNotFoundException, IOException {
		List<SuppliersA> suppliersAList = new ArrayList<>();
		try (CSVReader reader = new CSVReader(new FileReader(suppliersAFile))) {
			List<String[]> r = reader.readAll();
			r.forEach(e -> {
				SuppliersA suppliersA = new SuppliersA();
				suppliersA.setId(e[0]);
				suppliersA.setName(e[1]);
				suppliersAList.add(suppliersA);
			});
		}
		return suppliersAList;
	}

	private static List<CatalogB> getCatalogB(String CatalogBFile) throws FileNotFoundException, IOException {
		List<CatalogB> catalogBList = new ArrayList<>();
		try (CSVReader reader = new CSVReader(new FileReader(CatalogBFile))) {
			List<String[]> r = reader.readAll();
			r.forEach(e -> {
				CatalogB catalogB = new CatalogB();
				catalogB.setSku(e[0]);
				catalogB.setDescription(e[1]);
				catalogBList.add(catalogB);
			});
		}
		return catalogBList;
	}

	private static List<CatalogA> getCatalogA(String catalogAFile) throws FileNotFoundException, IOException {
		List<CatalogA> catalogAList = new ArrayList<>();
		try (CSVReader reader = new CSVReader(new FileReader(catalogAFile))) {
			List<String[]> r = reader.readAll();
			r.forEach(e -> {
				CatalogA catalogA = new CatalogA();
				catalogA.setSku(e[0]);
				catalogA.setDescription(e[1]);
				catalogAList.add(catalogA);
			});
		}
		return catalogAList;
	}

	private static List<BarCodesA> getBarCodesA(String barcodesA) throws FileNotFoundException, IOException {
		List<BarCodesA> barcodeAList = new ArrayList<>();
		try (CSVReader reader = new CSVReader(new FileReader(barcodesA))) {
			List<String[]> r = reader.readAll();
			r.forEach(e -> {
				BarCodesA codeA = new BarCodesA();
				codeA.setSupplierID(e[0]);
				codeA.setSku(e[1]);
				codeA.setBarcode(e[2]);
				barcodeAList.add(codeA);
			});
		}
		return barcodeAList;
	}

	private static List<BarCodesB> getBarCodesB(String barcodesB) throws FileNotFoundException, IOException {
		List<BarCodesB> barcodeBList = new ArrayList<>();
		try (CSVReader reader = new CSVReader(new FileReader(barcodesB))) {
			List<String[]> r = reader.readAll();
			r.forEach(e -> {
				BarCodesB codeB = new BarCodesB();
				codeB.setSupplierID(e[0]);
				codeB.setSku(e[1]);
				codeB.setBarcode(e[2]);
				barcodeBList.add(codeB);
			});
		}
		return barcodeBList;
	}

}
