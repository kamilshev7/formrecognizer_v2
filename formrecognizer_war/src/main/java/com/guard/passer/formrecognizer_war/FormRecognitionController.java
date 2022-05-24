package com.guard.passer.formrecognizer_war;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.ai.formrecognizer.FormRecognizerClient;
import com.azure.ai.formrecognizer.FormRecognizerClientBuilder;
import com.azure.ai.formrecognizer.models.FieldValueType;
import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class FormRecognitionController {

    @Autowired
    private RecognitionResultsRepository resultsRepository;

    @Value("${azure.form.recognizer.key}")
    private String key;

    @Value("${azure.form.recognizer.endpoint}")
    private String endpoint;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/upload")
    public String upload() {
        return "upload";
    }

    @GetMapping("/results")
    public ModelAndView results() {
        List<RecognitionResult> recognitionResults = resultsRepository.findAll();

        ModelAndView modelAndView = new ModelAndView("results");
        modelAndView.addObject("recognitionResults", recognitionResults);

        return modelAndView;
    }

    @GetMapping("/results/{id}")
    public ModelAndView resultsForOneL(@PathVariable Long id) {
        RecognitionResult recognitionResult = resultsRepository.getById(id);
        ModelAndView modelAndView = new ModelAndView("resultsForOne");
        modelAndView.addObject("recognitionResult", recognitionResult);

        return modelAndView;
    }

    @GetMapping("/delete/{id}")
    public ModelAndView resultDelete(@PathVariable Long id){
        resultsRepository.deleteById(id);

        List<RecognitionResult> recognitionResults = resultsRepository.findAll();
        ModelAndView modelAndView = new ModelAndView("results");
        modelAndView.addObject("recognitionResults", recognitionResults);

        return modelAndView;
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {
        // Create FormRecognizerClient
        FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder()
                .credential(new AzureKeyCredential(key))
                .endpoint(endpoint)
                .buildClient();

        try (InputStream receiptImage = file.getInputStream()) {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                    formRecognizerClient.beginRecognizeReceipts(receiptImage, file.getSize());

            List<RecognizedForm> recognizedForms = syncPoller.getFinalResult();

            // Check if we have at least one form
            if(recognizedForms.size() >= 1) {
                // Get recognized form
                final RecognizedForm recognizedForm = recognizedForms.get(0);

                // Extract fields
                RecognitionResult recognitionResult = ExtractFormFields(file, recognizedForm);

                // Store result
                resultsRepository.save(recognitionResult);

                // Debut results
                System.out.println("\n\n--== Recognition result ==--\n\n" + recognitionResult.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "index";
    }

    private RecognitionResult ExtractFormFields(MultipartFile file, final RecognizedForm recognizedForm) {
        RecognitionResult recognitionResult = new RecognitionResult();

        Receipt receipt = new Receipt(recognizedForm);

        // Set receipt file name based on the upload image name
        recognitionResult.setReceiptFileName(file.getOriginalFilename());

        // Get Merchant name and transaction date
        recognitionResult.setMerchantName(receipt.getMerchantName().getValue());
        recognitionResult.setTransactionDate(receipt.getTransactionDate().getValue());

//        recognitionResult.setTotal(receipt.getTotal().getValue());


        // Retrieve total
        Map<String, FormField> recognizedFields = recognizedForm.getFields();
        FormField totalField = recognizedFields.get("Total");
        if (totalField != null) {
            if (FieldValueType.FLOAT == totalField.getValue().getValueType()) {
                recognitionResult.setTotal(totalField.getValue().asFloat());
            }
        }
        return recognitionResult;
    }

    @GetMapping("/chart")
    public String chart() {
        return "chart";
    }

    @RequestMapping(value = "/getChartData", method = RequestMethod.GET)
    @ResponseBody
    public String getChartData() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        List<Double> items = resultsRepository.findAll().stream().map(
                r -> r.getTotal()).collect(Collectors.toList());

        return objectMapper.writeValueAsString(items);
    }
}