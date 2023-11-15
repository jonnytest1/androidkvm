package com.example.jonathan.androidkvm;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {


        int s=(int)'ß';
        String str=new String(new int[]{s},0,1);
        assertEquals('ß', str);
    }
}