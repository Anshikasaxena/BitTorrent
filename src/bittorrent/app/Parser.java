package bittorrent.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bittorrent.ParserOption;

public class Parser
{
	private List<String> argsList;
	private List<ParserOption> optsList;
	private List<String> doubleOptsList;

	public static Parser parser(String[] args)
	{
//		args = new String[]{
//			"-peers",
//			"thisfile.cfg",
//			"-common", 
//			"commonfile.cfg",
//			"-server",
//			"localhost",
//			"-numofpeers",
//			"10",
//			"-startport",
//			"6008",
//			"-startpeer",
//			"100",
//			"-shared",
//			"alice.txt",
//			"-numprefneighbors",
//			"4",
//			"-unchokingint",
//			"5",
//			"-optunchokeint",
//			"15",
//			"-pieceSize",
//			"8096"
//		};
		Parser parser = new Parser();

		parser.argsList = new ArrayList<String>();
		parser.optsList = new ArrayList<ParserOption>();
		parser.doubleOptsList = new ArrayList<String>();

		for (int i = 0; i < args.length; i++)
		{
			switch (args[i].charAt(0))
			{
			case '-':
				if (args[i].length() < 2)
				{
					throw new IllegalArgumentException("Not a valid argument: " + args[i]);
				}
				if (args[i].charAt(1) == '-')
				{
					if (args[i].length() < 3)
					{
						throw new IllegalArgumentException("Not a valid argument: " + args[i]);
					}
					parser.doubleOptsList.add(args[i].substring(2, args[i].length()));
				}
				else
				{
					if (args.length - 1 == i)
					{
						throw new IllegalArgumentException("Expected arg after: " + args[i]);
					}
					BitTorrentOption bitOption = BitTorrentOption.parse(args[i]);
					ParserOption option = new ParserOption(bitOption, args[i + 1]);
					parser.optsList.add(option);
					i++;
				}
				break;
			default:
				parser.argsList.add(args[i]);
				break;
			}
		}
		for (ParserOption option : parser.optsList)
		{
			System.out.println(option.toString());
		}
		return parser;
	}

	public List<String> getArgsList()
	{
		return this.argsList;
	}

	public List<ParserOption> getOptsList()
	{
		return this.optsList;
	}

	public HashMap<BitTorrentOption, String> getOptionsMap()
	{
		HashMap<BitTorrentOption, String> map = new HashMap<BitTorrentOption, String>();
		for (ParserOption option : this.optsList)
		{
			map.put(option.getFlag(), option.getOption());
		}
		return map;
	}

	public List<String> getDoubleOptsList()
	{
		return this.doubleOptsList;
	}
}
