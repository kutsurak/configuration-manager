/**
 * TwitterSearch - A wrapper library around the search twitter API with filtering.
 * Copyright (C) 2012 Panagiotis Koutsourakis <panagiotis.koutsourakis@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.slartibartfast.configurationmanager;

import java.io.IOException;

import java.util.Map;
import java.util.TreeMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

public class ConfigurationManager {
    private String filename_;
    private Map<String, Map<String, String>> sections_;

    public ConfigurationManager(String filename) {
        filename_ = filename;
        sections_ = new TreeMap<>();
    }

    public void parseFile() throws ParseError {
        int line_no = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(filename_)));
            Map<String, String> current_section = new TreeMap<>();
            sections_.put("default", current_section);
            String line = null;
            while((line = reader.readLine()) != null) {
                String pr_line = line.trim();
                line_no++;

                //ignore empty lines and lines starting with '#'
                if (pr_line.isEmpty() || pr_line.charAt(0) == '#')
                    continue;

                //ignore everything after a # character
                int hash_index = pr_line.indexOf('#');
                if (hash_index != -1 && isComment(pr_line, hash_index)) {
                    pr_line = pr_line.substring(0, hash_index);
                }

                //section header
                if (pr_line.startsWith("[")) {
                    if (pr_line.indexOf(']') == -1) {
                        throw new ParseError("Unterminated section header at line " +
                                             line_no + ": " + line);
                    }

                    String section_name = pr_line.substring(1, pr_line.indexOf(']'));
                    // If the section already exists get it
                    if (sections_.containsKey(section_name)) {
                        current_section = sections_.get(section_name);
                    }
                    else {
                        current_section = new TreeMap<>();
                        sections_.put(section_name, current_section);
                    }
                }
                else {
                    String kv_pair[] = pr_line.split("\\s*=\\s*");
                    if (kv_pair.length != 2) {
                        throw new ParseError("Invalid key value pair at line" +
                                             line_no + ": " + line);
                    }
                    current_section.put(kv_pair[0], kv_pair[1]);
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public String getProperty(String section_name, String property)
            throws PropertyNotFoundException {
        if (!sections_.containsKey(section_name)) {
            throw new PropertyNotFoundException("Section " + section_name + " not found");
        }
        Map<String, String> section = sections_.get(section_name);
        if (!section.containsKey(property))
            throw new PropertyNotFoundException("Property " + property + " not found in section " + section_name);
        return section.get(property);
    }

    public void printConfiguration() {
        for (Map.Entry<String, Map<String, String>> section: sections_.entrySet()) {
            System.out.println(section.getKey());
            for(Map.Entry<String, String> entry: section.getValue().entrySet()) {
                System.out.println("  " + entry.getKey() + " : " + entry.getValue());
            }
        }
    }
    /* Possibly for future expansion that takes into account strings,
     * escaped characters etc.
     */
    public boolean isComment(String line, int hash_index) {
        return true;
    }
}
