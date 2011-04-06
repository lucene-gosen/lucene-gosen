/*
 * Copyright (C) 2002-2007
 * Takashi Okamoto <tora@debian.org>
 * Matt Francis <asbel@neosheffield.co.uk>
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package net.java.sen.dictionary;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A class representing part-of-speech data for a morpheme. When created during
 * the analysis of a string, a Morpheme is built as a lazy proxy onto the
 * Dictionary's part-of-speech file. Once created, a <code>Morpheme</code> can
 * be altered without changing the underlying data (if any)
 * 
 *  <p><b>Thread Safety</b>: Objects of this class are <b>NOT</b> thread safe and
 * should not be accessed simultaneously by multiple threads.
 * 
 *  <p><b>CAUTION</b>: {@link Morpheme}s are implemented as lazy proxies onto a
 *  {@link Dictionary}, and care should be taken not to access the same
 *  {@link Dictionary} from multiple threads. Once any member of a
 *  {@link Morpheme} has been read, its link to the {@link Dictionary} is broken
 *  and this restriction is relaxed
 */
public class Morpheme {

	/**
	 * The {@link Dictionary} that contains this <code>Morpheme</code>
	 */
	private Dictionary dictionary;

	/**
	 * The index of this part-of-speech within the part-of-speech information
	 * file
	 */
	private int partOfSpeechIndex;

	/**
	 * Indicates if the part-of-speech data has been loaded
	 */
	private boolean loaded;

	/**
	 * The conjugation type of the morpheme
	 */
	private String conjugationalType = null;

	/**
	 * The conjugation form of the morpheme
	 */
	private String conjugationalForm = null;

	/**
	 * The unconjugated form of the morpheme
	 */
	private String basicForm = null;

	/**
	 * The readings of the morpheme
	 */
	private List<String> readings = null;

	/**
	 * The pronunciations of the morpheme
	 */
	private List<String> pronunciations = null;

	/**
	 * The part-of-speech in Chasen format
	 */
	private String partOfSpeech = null;

	/**
	 * Arbitrary additional information
	 */
	private String additionalInformation = null;


	/**
	 * Loads the part-of-speech data from the {@link Dictionary}
	 *
	 */
	private void load() {

		if (!this.loaded) {

			CharBuffer buffer = this.dictionary.getPartOfSpeechInfoBuffer();
			buffer.position(this.partOfSpeechIndex);
			char[] temp = new char[512];
			int length;

			length = buffer.get();
			buffer.get(temp, 0, length);
			this.partOfSpeech = new String(temp, 0, length);

			length = buffer.get();
			buffer.get(temp, 0, length);
			this.conjugationalType = new String(temp, 0, length);

			length = buffer.get();
			buffer.get(temp, 0, length);
			this.conjugationalForm = new String(temp, 0, length);

			length = buffer.get();
			buffer.get(temp, 0, length);
			this.basicForm = new String(temp, 0, length);

			int numReadings = buffer.get();

			this.readings = new ArrayList<String>(numReadings);
			for (int i = 0; i < numReadings; i++) {
				length = buffer.get();
				buffer.get(temp, 0, length);
				this.readings.add(new String(temp, 0, length));
			}
			this.pronunciations = new ArrayList<String>(numReadings);
			for (int i = 0; i < numReadings; i++) {
				length = buffer.get();
				buffer.get(temp, 0, length);
				this.pronunciations.add(new String(temp, 0, length));
			}

			this.loaded = true;

		}

	}


	/**
	 * Gets the conjugation type of the morpheme
	 * 
	 * @return The conjugation type
	 */
	public String getConjugationalType() {

		if (!this.loaded) {
			this.load();
		}

		return this.conjugationalType;

	}


	/**
	 * Sets the conjugation type of the morpheme
	 * 
	 * @param conjugationalType The conjugation type
	 */
	public void setConjugationalType(String conjugationalType) {

		if (!this.loaded) {
			this.load();
		}

		this.conjugationalType = conjugationalType;

	}


	/**
	 * Gets the conjugation form of the morpheme
	 * 
	 * @return The conjugation form
	 */
	public String getConjugationalForm() {

		if (!this.loaded) {
			this.load();
		}

		return this.conjugationalForm;

	}


	/**
	 * Sets the conjugation form of the morpheme
	 * 
	 * @param conjugationalForm The conjugation form
	 */
	public void setConjugationalForm(String conjugationalForm) {

		if (!this.loaded) {
			this.load();
		}

		this.conjugationalForm = conjugationalForm;

	}


	/**
	 * Gets the unconjugated form of the morpheme
	 * 
	 * @return The unconjugated form
	 */
	public String getBasicForm() {

		if (!this.loaded) {
			this.load();
		}

		return this.basicForm;

	}


	/**
	 * Sets the unconjugated form of the morpheme
	 * 
	 * @param basicString The unconjugated form 
	 */
	public void setBasicForm(String basicString) {

		if (!this.loaded) {
			this.load();
		}

		this.basicForm = basicString;

	}


	/**
	 * Gets the readings of the morpheme
	 * 
	 * @return The readings
	 */
	public List<String> getReadings() {

		if (!this.loaded) {
			this.load();
		}

		return this.readings;

	}


	/**
	 * Sets the readings of the morpheme
	 * 
	 * @param readings The readings
	 */
	public void setReadings(List<String> readings) {

		if (!this.loaded) {
			this.load();
		}

		this.readings = new ArrayList<String>(readings);

	}


	/**
	 * Gets the pronunciations of the morpheme
	 * 
	 * @return The pronunciations
	 */
	public List<String> getPronunciations() {

		if (!this.loaded) {
			this.load();
		}

		return this.pronunciations;

	}

	
	/**
	 * Sets the pronunciation of the morpheme

	 * @param pronunciations the pronunciations
	 */
	public void setPronunciations(List<String> pronunciations) {

		if (!this.loaded) {
			this.load();
		}

		this.pronunciations = new ArrayList<String>(pronunciations);

	}

	
	/**
	 * Gets the part-of-speech in Chasen format
	 * 
	 * @return The part-of-speech in Chasen format
	 */
	public String getPartOfSpeech() {

		if (!this.loaded) {
			this.load();
		}

		return this.partOfSpeech;

	}


	/**
	 * Sets the part-of-speech
	 * 
	 * @param partOfSpeech The part-of-speech
	 */
	public void setPartOfSpeech(String partOfSpeech) {

		if (!this.loaded) {
			this.load();
		}

		this.partOfSpeech = partOfSpeech;

	}


	/**
	 * Gets the additional information string
	 * 
	 * @return The additional information string
	 */
	public String getAdditionalInformation() {
		return this.additionalInformation;
	}


	/**
	 * Sets an arbitrary string of additional information
	 * 
	 * @param additionalInformation The additional information to set
	 */
	public void setAdditionalInformation(String additionalInformation) {
		this.additionalInformation = additionalInformation;
	}


	/**
	 * Compare two lists of strings
	 *
	 * @param list1 The first list
	 * @param list2 The second list
	 * @return <code>true</code> if the lists' contents are exactly equal
	 */
	private boolean stringListsEqual(List<String> list1, List<String> list2) {

		if (list1 == list2) {
			return true;
		}

		if ((list1 == null) || (list2 == null)) {
			return false;
		}

		if (list1.size() != list2.size()) {
			return false;
		}

		for (int i = 0; i < list1.size(); i++) {
			if (!list1.get(i).equals(list2.get(i))) {
				return false;
			}
		}

		return true;

	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {

		if (object instanceof Morpheme) {

			Morpheme morpheme = (Morpheme) object;

			String partOfSpeech = this.getPartOfSpeech();
			String conjugationalType = this.getConjugationalType();
			String conjugationalForm = this.getConjugationalForm();
			String basicForm = this.getBasicForm();
			List<String> pronunciations = this.getPronunciations();
			List<String> readings = this.getReadings();
			String additionalInformation = this.getAdditionalInformation();

			String otherPartOfSpeech = morpheme.getPartOfSpeech();
			String otherConjugationalType = morpheme.getConjugationalType();
			String otherConjugationalForm = morpheme.getConjugationalForm();
			String otherBasicForm = morpheme.getBasicForm();
			List<String> otherPronunciations = morpheme.getPronunciations();
			List<String> otherReadings = morpheme.getReadings();
			String otherAdditionalInformation = morpheme.getAdditionalInformation();

			if (
					   ((basicForm == otherBasicForm) || (basicForm != null && basicForm.equals(otherBasicForm)))
					&& ((conjugationalType == otherConjugationalType) || (conjugationalType != null && conjugationalType.equals(otherConjugationalType)))
					&& ((conjugationalForm == otherConjugationalForm) || (conjugationalForm != null && conjugationalForm.equals(otherConjugationalForm)))
					&& ((partOfSpeech == otherPartOfSpeech) || (partOfSpeech != null && partOfSpeech.equals(otherPartOfSpeech)))
					&& (stringListsEqual(pronunciations, otherPronunciations))
					&& (stringListsEqual(readings, otherReadings))
					&& ((additionalInformation == otherAdditionalInformation) || (additionalInformation != null && additionalInformation.equals(otherAdditionalInformation)))					
			   )
			{
				return true;
			}

		}

		return false;

	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		if (!this.loaded) {
			this.load();
		}

		StringBuilder partOfSpeechData = new StringBuilder();
		String[] parts = this.partOfSpeech.split("-");
		for (int i = 0; i < 4; i++) {
			if (i < parts.length) {
				partOfSpeechData.append(parts[i]);
			} else {
				partOfSpeechData.append("*");
			}
			partOfSpeechData.append(",");
				
		}

		partOfSpeechData.append(this.conjugationalType);
		partOfSpeechData.append(",");
		partOfSpeechData.append(this.conjugationalForm);
		partOfSpeechData.append(",");
		partOfSpeechData.append(this.basicForm);
		partOfSpeechData.append(",");
		partOfSpeechData.append((this.readings.size() > 0) ? this.readings.get(0) : "null");
		partOfSpeechData.append(",");
		partOfSpeechData.append((this.pronunciations.size() > 0) ? this.pronunciations.get(0) : "null");
		return partOfSpeechData.toString();

	}


	/**
	 * Builds a lazy proxy onto a part-of-speech stored in a Dictionary
	 * 
	 * @param dictionary The dicationary to proxy upon
	 * @param partOfSpeechIndex The index into the part-of-speech file
	 */
	public Morpheme(Dictionary dictionary, int partOfSpeechIndex) {

		this.dictionary = dictionary;
		this.partOfSpeechIndex = partOfSpeechIndex;
		this.loaded = false;

	}


	/**
	 * Creates a literal <code>Morpheme</code> that does not link to any
	 * Dictionary
	 * @param partOfSpeech The Chasen-format part-of-speech
	 * @param conjugationalType The conjugational type
	 * @param conjugationalForm The conjugational form
	 * @param basicForm The unconjugated form
	 * @param readings The readings
	 * @param pronunciations The pronunciations
	 * @param additionalInformation Arbitrary additional information
	 */
	public Morpheme(String partOfSpeech, String conjugationalType, String conjugationalForm, String basicForm, String[] readings, String[] pronunciations, String additionalInformation) {

		this.basicForm = basicForm;
		this.conjugationalType = conjugationalType;
		this.conjugationalForm = conjugationalForm;
		this.readings = new ArrayList<String>(Arrays.asList(readings));
		this.pronunciations = new ArrayList<String>(Arrays.asList(pronunciations));
		this.partOfSpeech = partOfSpeech;
		this.additionalInformation = additionalInformation;

		this.loaded = true;

	}


	/**
	 * Creates a blank, modifiable <code>Morpheme</code> that does not link
	 * to any Dictionary
	 */
	public Morpheme() {

		this.readings = new ArrayList<String>();
		this.pronunciations = new ArrayList<String>();
		this.loaded = true;

	}


}
