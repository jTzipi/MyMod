package eu.jpangolin.jtzipi.mymod.io.async;

/**
 * Try to look for files in dirs using a 'balanced' .
 * <p>
 *     We first search for all directories and put them
 *     in several lists of threads. So that each thread can
 *     consume his list of dirs. Searching for files. And if the
 *     list is empty maybe steal from an others threads list.
 *
 *
 * </p>
 *
 * @author jTzipi
 */
public class BalancedPathCrawler {
}