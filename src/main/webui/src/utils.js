export const generateRandomTasks = ({numberOfTasks, totalTaskCount, possibleSkills, icons}) => {
  const tasks = [];
  for (let i = 0; i < numberOfTasks; i++) {
    const numSkills = Math.floor(Math.random() * 3) + 1; // 1-3 skills
    const selectedSkills = possibleSkills
      .map(skill => skill.id)
      .sort(() => 0.5 - Math.random())
      .slice(0, numSkills);

    const licensePlate = Math.random().toString(36).substring(2, 8).toUpperCase();
    const iconCls = `b-fa b-fa-${icons[Math.floor(Math.random() * icons.length)]}`;

    tasks.push({
      name: `Task ${totalTaskCount + i + 1}`,
      duration: Math.floor(Math.random() * 8) + 1, // 1-8 hours
      skills: selectedSkills,
      licensePlate,
      iconCls
    });
  }
  return tasks;
};