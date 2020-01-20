package bittorrent;

import bittorrent.app.BitTorrentOption;

public class ParserOption
{
	private BitTorrentOption flag;
	private String option;
	
	public ParserOption(BitTorrentOption flag, String option)
	{
		this.flag = flag;
		this.option = option;
	}
	
	public BitTorrentOption getFlag()
	{
		return this.flag;
	}
	
	public String getOption()
	{
		return this.option;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder("flag: ");
		builder.append(this.flag);
		builder.append("   option: ");
		builder.append(this.option);
		return builder.toString();
	}
}
