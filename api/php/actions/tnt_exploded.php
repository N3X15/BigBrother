<?php

class TNTExploded extends Action
{
	public function __construct($row)
	{
		parent::LoadData($row);
	}
	
	public function __toString()
	{
		return sprintf("TNT exploded at World %d - &lt;%d,%d,%d&gt;",$this->data, $this->world,$this->X,$this->Y,$this->Z);
	}
	
	public function getActionString()
	{
		return 'TNT exploded';
	}
}