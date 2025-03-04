package com.adobe.convertor.controller;

/*
 * @project number-to-roman
 * @author jayakesavanmuthazhagan
 * @created - Jul, 25 2024 - 05:16 AM
 */


import com.adobe.convertor.bean.ConversionResponse;
import com.adobe.convertor.bean.ConversionResult;
import com.adobe.convertor.exception.InvalidInputException;
import com.adobe.convertor.service.NumberToRomanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(NumberToRomanController.class)
public class NumberToRomanControllerTest {

    @MockBean
    private NumberToRomanService numberToRomanService;


    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        openMocks(this);
    }


    @Test
    public void testConvertToRoman_SingleNumber() throws Exception {
        when(numberToRomanService.convertToRomanNumeral(5)).thenReturn("V");

        mockMvc.perform(get("/romannumeral")
                        .param("query", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("V"));

        verify(numberToRomanService, times(1)).convertToRomanNumeral(5);
    }

    @Test
    public void testConvertToRoman_Range() throws Exception {
        ConversionResponse response = new ConversionResponse(
                List.of(new ConversionResult("1", "I"), new ConversionResult("2", "II"))
        );
        when(numberToRomanService.convertRangeToRoman(1, 2)).thenReturn(response.getConversions());

        mockMvc.perform(get("/romannumeral")
                        .param("min", "1")
                        .param("max", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"conversions\":[{\"input\":\"1\",\"output\":\"I\"},{\"input\":\"2\",\"output\":\"II\"}]}"));

        verify(numberToRomanService, times(1)).convertRangeToRoman(1, 2);
    }


    @Test
    public void testConvertToRoman_InvalidInput() throws Exception {
        mockMvc.perform(get("/romannumeral")
                        .param("query", "invalid")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(InvalidInputException.class, result.getResolvedException()));

        verify(numberToRomanService, never()).convertToRomanNumeral(anyInt());
    }

    @Test
    public void testConvertToRoman_InvalidRange() throws Exception {
        doThrow(new InvalidInputException("Invalid range. Ensure min < max and both are in the range 1-3999."))
                .when(numberToRomanService).convertRangeToRoman(5, 3);
        mockMvc.perform(get("/romannumeral")
                        .param("min", "5")
                        .param("max", "3")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(InvalidInputException.class, result.getResolvedException()));

        verify(numberToRomanService, never()).convertRangeToRoman(5, 3);
    }
}
