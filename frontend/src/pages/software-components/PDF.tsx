import jsPDF from "jspdf";

const generatePDF = (content: string[][]) => {
  const doc = new jsPDF();

  // Add text to PDF. This can be customized based on your content
  let currentYPosition = 10; // Initial Y position for the first line

  content.forEach((lineGroup) => {
    lineGroup.forEach((line) => {
      doc.text(line, 10, currentYPosition);
      currentYPosition += 10; // Increase Y position for each new line
    });

    currentYPosition += 10; // Optionally add extra space between groups
  });

  // Save the PDF with a filename
  doc.save("download.pdf");
};

export default generatePDF;
