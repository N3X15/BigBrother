<?php

class ChestOpened extends Action
{
	public function __construct($row)
	{
		parent::LoadData($row);
	}
	
	public function __toString()
	{
		return sprintf("%s opened a chest at World %s - &lt;%d,%d,%d&gt;",$this->getUserLink(),$this->data, $this->getWorldName(),$this->X,$this->Y,$this->Z);
	}
	
	public function getActionString()
	{
		return 'opened a chest';
	}
}