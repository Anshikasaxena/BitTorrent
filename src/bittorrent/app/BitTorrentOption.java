package bittorrent.app;

public enum BitTorrentOption
{
	PEERS("peers"),
	COMMON("common"),
	SERVER("server"),
	NUM_PEERS("numofpeers"),
	START_PORT("startport"),
	START_PEER("startpeer"),
	SHARED("shared"),
	NUM_PREF_NEIGHBORS("numprefneighbors"),
	UNCHOKE_INT("unchokingint"),
	OPT_UNCHOKE_INT("optunchokeint"),
	PIECE_SIZE("pieceSize");
	
	private String option;
	
	BitTorrentOption(String option)
	{
		this.option = option;
	}
	
	public static BitTorrentOption parse(String flag)
	{
		if (flag.startsWith("-"))
		{
			flag = flag.substring(1);
		}
		BitTorrentOption option = null;
		for (BitTorrentOption opt : BitTorrentOption.values())
		{
			if (opt.option.equalsIgnoreCase(flag))
			{
				option = opt;
				break;
			}
		}
		return option;
	}
}
