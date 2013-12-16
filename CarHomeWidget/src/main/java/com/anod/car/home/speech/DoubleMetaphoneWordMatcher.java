/*
 * Copyright 2011 Greg Milette and Adam Stroud
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.anod.car.home.speech;

import com.anod.car.home.utils.AppLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.commons.codec.language.Soundex;

import root.gast.speech.text.match.WordMatcher;

/**
 * encode strings using soundex
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class DoubleMetaphoneWordMatcher extends WordMatcher
{
    protected static DoubleMetaphone sMetaphone;
    
    static
    {
		sMetaphone = new DoubleMetaphone();
		sMetaphone.setMaxCodeLen(1000);
    }

    public DoubleMetaphoneWordMatcher(String... wordsIn)
    {
        this(Arrays.asList(wordsIn));
    }

    public DoubleMetaphoneWordMatcher(List<String> wordsIn)
    {
        super(encode(wordsIn));
    }
    
    @Override
    public boolean isIn(String word)
    {
        return super.isIn(encode(word));
    } 
    
    protected static List<String> encode(List<String> input)
    {
        List<String> encoded = new ArrayList<String>();
        for (String in : input)
        {
            encoded.add(encode(in.toLowerCase()));
        }
        return encoded;
    }

    private static String encode(String in)
    {
        String encoded = in;
        try
        {
            encoded = sMetaphone.encode(in);
			AppLog.d(in + " == " + encoded);
        }
        catch (IllegalArgumentException e)
        {
			AppLog.e("word encode exception", e);
        }
        return encoded;
    }
}

    
